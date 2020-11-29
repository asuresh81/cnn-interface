# Part 2: Make Prediction: Image quantification

#%%
import os
import sys
import random
import cv2
import zipfile

import numpy as np

import matplotlib.pyplot as plt
import matplotlib.image as mpimg

import tensorflow as tf
from tensorflow import keras

import skimage
from skimage import img_as_ubyte
from skimage import img_as_float
from skimage import morphology
from skimage.morphology import square
from skimage import measure

from scipy import ndimage
from scipy.ndimage import label, generate_binary_structure

#%%
def run_model(file_to_predict, cnn_model):

    model = keras.models.load_model(cnn_model)

    # notice: the image_path corresponds to certain image;
    ## for desinging user interface, image_path should be automatically as the imagepath of the image imported by the user.
    ## also could design for batch prediction
    Predicting_image_path = file_to_predict

    #related to whether the image normalized
    image_path = Predicting_image_path
    image = cv2.imread(image_path, 0)
    image = cv2.resize(image, (576, 576))
    image = img_as_float(image)
    image *= 255.0/image.max()
    image = image/255.0
    
    image = np.array(image)

    img = mpimg.imread(image_path)
    # imgplot = plt.imshow(img)
    
    y = np.expand_dims(image, axis=0)

    result = model.predict(y)
    result= np.squeeze(result, axis=0)
    result= np.squeeze(result, axis=-1)

    plt.imshow(result, cmap = 'gray', interpolation = 'bicubic')
    plt.xticks([]), plt.yticks([])
    dir_path = os.path.dirname(os.path.realpath(__file__))
    print("Output for dir path: " , dir_path)
    save_file = dir_path + "/" + "result_output_test.tif"
    plt.gca().set_axis_off()
    plt.subplots_adjust(top = 1, bottom = 0, right = 1, left = 0,
            hspace = 0, wspace = 0)
    plt.margins(0,0)
    plt.gca().xaxis.set_major_locator(plt.NullLocator())
    plt.gca().yaxis.set_major_locator(plt.NullLocator())
    plt.savefig(save_file, bbox_inches='tight', pad_inches=0)
    
    prediction_data = result
    
    # only show the boundary:
    ## Critical: must notice the output of CNN is pixel with continous numbers:
    # higher accuracy, the closer it is to the pixel values designated in masks.
    # thus, the number "0.6" made here is arbitary, depends on the accuracy of CNN, if Accuracy is high, it should be close to 1.0
    ### could influence the quantification result by thining or expending the boundary/membrane areas
    ## could also find ways to show other ROI predicted to make more options for the user.
    prediction_data[prediction_data >= 0.7] = 1
    prediction_data[prediction_data < 0.7] = 0
    
    prediction_data = prediction_data.astype('int')
    prediction_data = morphology.remove_small_objects(prediction_data.astype(bool),64)
    prediction_data = prediction_data.astype('float32')

    prediction_data = skimage.morphology.closing(prediction_data, square(3))

    ## add the row at the margin of image, because of bad annotation, the boundary in image margin would be miss classified.
    ROW= [0,1,2,3,4,5]
    for I in ROW:
      prediction_data[I,:]=1
      prediction_data[:,I]=1
      prediction_data[-I,:]=1
      prediction_data[:,-I]=1

    #change the interior area to value 1
    prediction_data[prediction_data == 1.0] = 0.5
    prediction_data[prediction_data == 0.0] = 1.0
    prediction_data[prediction_data == 0.5] = 0.0

    s = generate_binary_structure(2,2)

    # num_features
    # how many ROI/unconnected object it find
    labeled_array, num_features = label(prediction_data, structure=s)

    unique, counts = np.unique(labeled_array, return_counts=True)
    dict(zip(unique, counts))

    for i in range(num_features+1):
      if np.count_nonzero(labeled_array == i)>1000: #discard small ROI
        a = np.count_nonzero(labeled_array == i)
        # print('pixel area =',np.count_nonzero(labeled_array == i))
        b = ndimage.sum(image, labeled_array, index=[i])
        # print('pixel intensity =',ndimage.sum(image, labeled_array, index=[i]))
        arr_1 = (labeled_array == i).astype(int)
        a1=np.roll(arr_1, 8, axis=0)
        a2=np.roll(arr_1, -8, axis=0)
        a3=np.roll(arr_1, 8, axis=1)
        a4=np.roll(arr_1, -8, axis=1)
        a5=a1+a2+a3+a4
        a5[a5 > i-0.1] = 1
        c= ndimage.sum(image, a5, index=[1])-b
        # print('pixel intensity in boundary =',c)
        ## Critical: the value "8" in np.rool meas to expand the region i by 8 pixels
        ### the value "8" corresponds to the length of the cell membranes.
        ## Critical: the prediction accuracy of CNN also would influence the parameter used in np.rool:
        ### also related to the parameter in "#only show the boundary:" lines

        slice_x, slice_y = ndimage.find_objects(labeled_array == i)[0]
        roi = labeled_array[slice_x, slice_y]
        # plt.figure()
        # plt.imshow(roi)
        # print('------')
        # print(i)
        # print(slice_x)

#%%
if __name__ == "__main__":

  # run_model(".\\file_for_cnn.png", ".\\content\\")
  
  run_model(sys.argv[1], sys.argv[2])
