# Part 2: Make Prediction: Image quantification

# In[ ]:
import os
import sys
import random

#import numpy as np
#import cv2
#import matplotlib.pyplot as plt
import matplotlib.image as mpimg

#from skimage import img_as_float

import tensorflow as tf
from tensorflow import keras

import zipfile

import skimage
from skimage import img_as_ubyte
from skimage import img_as_float
from skimage import morphology
from skimage.morphology import square
from skimage import measure
from scipy import ndimage
from scipy.ndimage import label, generate_binary_structure

import numpy as np
import cv2
import matplotlib.pyplot as plt

def run_model(file_to_predict, cnn_model):
    #model = keras.models.load_model('/Users/adityasuresh/comp523/image_analysis-master/content')

    model = keras.models.load_model(cnn_model)

    # In[ ]:


    #import image and normalize it:
    #Predicting_image_path='/Users/adityasuresh/comp523/image_analysis-master/content/vps26a-2 RGS1-YFP 0 min 6_ gluc 5.tif'
    #Predicting_image_path='/Users/adityasuresh/z_stack_timecourse_example.tif'
    Predicting_image_path = file_to_predict
    #notice: the image_path corresponds to certain image;
    ##for desinging user interface, image_path should be automatically as the imagepath of the image imported by the user.
    ##also could design for batch prediction

    image_path=Predicting_image_path
    image = cv2.imread(image_path, 0)
    image = cv2.resize(image, (576, 576))
    image=img_as_float(image)
    image *= 255.0/image.max()
    image=image/255.0
    #related to whether the image normalized
    image = np.array(image)

    img=mpimg.imread(image_path)
    imgplot = plt.imshow(img)
    #plt.show(block=True)

    # In[ ]:


    #print(image.shape)


    # In[ ]:


    y = np.expand_dims(image, axis=0)
    #print("y shape", y.shape)


    # In[ ]:


    result = model.predict(y)
    #result.shape


    # In[ ]:


    #img=mpimg.imread(image_path)
    #imgplot = plt.imshow(img)
    #plt.show()


    # In[ ]:


    #fig = plt.figure()
    #fig.subplots_adjust(hspace=0.4, wspace=0.4)

    #ax = fig.add_subplot(1, 2, 2)
    #ax.imshow(np.reshape(result[0]*255, (image_size, image_size)), cmap="gray")


    # Image quantificaton:

    # In[ ]:


    result= np.squeeze(result, axis=0)


    # In[ ]:


    #result.shape


    # In[ ]:


    result= np.squeeze(result, axis=-1)


    # In[ ]:


    #result.shape


    # In[ ]:


    #result


    # In[ ]:


    plt.imshow(result, cmap = 'gray', interpolation = 'bicubic')
    plt.xticks([]), plt.yticks([])
    dir_path = os.path.dirname(os.path.realpath(__file__))
    print("Output for dir path: " , dir_path)
    save_file = dir_path + "/" + "result_output_test.tif"
    plt.savefig(save_file, bbox_inches='tight')
    #plt.savefig('result_output_test.tif', bbox_inches='tight')
    #plt.savefig('/Users/adityasuresh/comp523/image_analysis-master/result_output_test.tif', bbox_inches='tight')
    #plt.show(block=True)


    # In[ ]:


    prediction_data=result


    # In[ ]:


    #only show the boundary:
    prediction_data[prediction_data >= 0.7] = 1
    prediction_data[prediction_data < 0.7] = 0

    ## Critical: must notice the output of CNN is pixel with continous numbers:
    # higher accuracy, the closer it is to the pixel values designated in masks.
    # thus, the number "0.6" made here is arbitary, depends on the accuracy of CNN, if Accuracy is high, it should be close to 1.0
    ### could influence the quantification result by thining or expending the boundary/membrane areas

    ## could also find ways to show other ROI predicted to make more options for the user.


    # In[ ]:


    #plt.imshow(prediction_data, cmap = 'gray', interpolation = 'bicubic')
    #plt.xticks([]), plt.yticks([])
    #plt.show()


    # In[ ]:


    #print("pred data: ", prediction_data)


    # In[ ]:


    prediction_data = prediction_data.astype('int')
    prediction_data = morphology.remove_small_objects(prediction_data.astype(bool),64)
    prediction_data = prediction_data.astype('float32')


    # In[ ]:


    prediction_data = skimage.morphology.closing(prediction_data, square(3))


    # In[ ]:


    ROW= [0,1,2,3,4,5]
    for I in ROW:
      prediction_data[I,:]=1
      prediction_data[:,I]=1
      prediction_data[-I,:]=1
      prediction_data[:,-I]=1

    ## add the row at the margin of image, because of bad annotation, the boundary in image margin would be miss classified.


    # In[ ]:


    prediction_data[prediction_data == 1.0] = 0.5
    prediction_data[prediction_data == 0.0] = 1.0
    prediction_data[prediction_data == 0.5] = 0.0

    #change the interior area to value 1


    # In[ ]:


    s = generate_binary_structure(2,2)


    # In[ ]:


    labeled_array, num_features = label(prediction_data, structure=s)


    # In[ ]:


    #num_features
    #how many ROI/unconnected object it find


    # In[ ]:


    #plt.imshow(labeled_array)
    #different colors conrespond to different labels (numbers)


    # In[ ]:


    unique, counts = np.unique(labeled_array, return_counts=True)
    dict(zip(unique, counts))
    #label (number) has 26669 pixels


    # In[ ]:


    for i in range(num_features+1):
      if np.count_nonzero(labeled_array == i)>1000: #discard small ROI
        a = np.count_nonzero(labeled_array == i)
        #print('pixel area =',np.count_nonzero(labeled_array == i))
        b = ndimage.sum(image, labeled_array, index=[i])
        #print('pixel intensity =',ndimage.sum(image, labeled_array, index=[i]))
        arr_1 = (labeled_array == i).astype(int)
        a1=np.roll(arr_1, 8, axis=0)
        a2=np.roll(arr_1, -8, axis=0)
        a3=np.roll(arr_1, 8, axis=1)
        a4=np.roll(arr_1, -8, axis=1)
        a5=a1+a2+a3+a4
        a5[a5 > i-0.1] = 1
        c= ndimage.sum(image, a5, index=[1])-b
        #print('pixel intensity in boundary =',c)
        ##Critical: the value "8" in np.rool meas to expand the region i by 8 pixels
        ## the value "8" corresponds to the length of the cell membranes.
        ##Critical: the prediction accuracy of CNN also would influence the parameter used in np.rool:
        ### also related to the parameter in "#only show the boundary:" lines

        slice_x, slice_y = ndimage.find_objects(labeled_array == i)[0]
        roi = labeled_array[slice_x, slice_y]
        #plt.figure()
        #plt.imshow(roi)
        #print('------')
        #print(i)
        #print(slice_x)

if __name__ == "__main__":
    # If statement is unnecessary, just haven't gotten rid of it yet.
    if(len(sys.argv) > 1):
        predict_image = sys.argv[1]
        cnn_model_folder = sys.argv[2]
        #print(predict_image)
        #print(cnn_model)
        run_model(predict_image, cnn_model_folder)
