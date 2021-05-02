
# coding: utf-8

# **Import Library (check duplication)**

# In[1]:


import skimage
from skimage import img_as_ubyte
from skimage import img_as_float
from skimage import morphology
from skimage.morphology import square
from skimage import measure
import scipy
from scipy import ndimage
from scipy.ndimage import label, generate_binary_structure

import numpy as np

import cv2 as cv


# In[2]:


import os
import sys
np.set_printoptions(threshold=sys.maxsize)
import random
import matplotlib.pyplot as plt
import matplotlib.image as mpimg

import tensorflow as tf
from tensorflow import keras


# In[3]:


from copy import deepcopy


# In[4]:


from PIL import Image


# **CNN prediction:**

# In[5]:

def run_model(image_to_predict, cnn_model):
  # load the saved CNN model (boundary_only)
  #model = keras.models.load_model('/Users/adityasuresh/comp523/cnn-interface/models/joneslab_model/content')
  model = keras.models.load_model(cnn_model)

  # In[9]:


  #import image and normalize it:
  #Predicting_image_path='/Users/adityasuresh/Downloads/sample.tif'
  Predicting_image_path = image_to_predict
  #notice: the image_path corresponds to certain image;
  ##for desinging user interface, image_path should be automatically as the imagepath of the image imported by the user.
  ##also could design for batch prediction

  image_path=Predicting_image_path
  image = plt.imread(image_path)
  image = image[..., :3]#delete the alpha chanel
  image = np.delete(image, np.s_[::2], 2)#pamerater here reduc the third dimesion value (keep the Green chanel)
  image = np.squeeze(image, axis=-1)
  image = cv.resize(image, (576, 576))
  image=img_as_float(image)
  image *= 255.0/image.max()
  image=image/255.0
  #related to whether the image normalized
  image = np.array(image)


  # In[11]:


  #Check image shape
  #image.shape


  # In[12]:


  # Expand image shape to be readable by CNN
  y = np.expand_dims(image, axis=0)
  #y.shape


  # In[13]:


  # CNN prediction
  result = model.predict(y)
  #result.shape


  # In[14]:


  # Check the original image
  # img=mpimg.imread(image_path)
  # imgplot = plt.imshow(img)
  # plt.show()


  # In[15]:


  # check the result
  # image_to_show = np.reshape(result[0]*255, (576, 576))
  # imgplot = plt.imshow(image_to_show)
  # plt.show()


  # **Image Segementation:**

  # In[16]:


  # Reduce image dimension
  result= np.squeeze(result, axis=0)


  # In[17]:


  #result.shape


  # In[18]:


  # reduce image dimension
  result= np.squeeze(result, axis=-1)


  # In[19]:


  #result.shape


  # In[20]:


  # Check the boundary mask predicted
  # plt.imshow(result, cmap = 'gray', interpolation = 'bicubic')
  # plt.xticks([]), plt.yticks([])
  # plt.colorbar ()
  # plt.show()


  # In[21]:


  prediction_data=result


  # In[22]:


  #only show the boundary:
  prediction_data[prediction_data >= 0.1] = 1
  prediction_data[prediction_data < 0.1] = 0
  # threshold here will be determined experimentally


  # In[23]:


  # Check the image of prediction data
  # plt.imshow(prediction_data, cmap = 'gray', interpolation = 'bicubic')
  # plt.xticks([]), plt.yticks([])
  # plt.show()


  # In[24]:


  #prediction_data


  # In[25]:


  prediction_data = prediction_data*255

  prediction_data = prediction_data.astype(np.uint8)


  # In[26]:


  #prediction_data


  # In[27]:


  ## Also could save it as image in the file
  #im = Image.fromarray(prediction_data)
  #im.save("POST_CNN_MASK.png")


  # In[28]:


  # dilation:
  kernel = np.ones((5,5),np.uint8)
  dilation = cv.dilate(prediction_data,kernel,iterations = 1)


  # In[29]:


  #dilation


  # In[30]:


  #Check the dilation:
  # plt.imshow(dilation, cmap = 'gray', interpolation = 'bicubic')
  # plt.xticks([]), plt.yticks([])
  # plt.show()


  # In[31]:


  # Erosion:
  erosion = cv.erode(dilation,kernel,iterations = 1)


  # In[32]:


  # Check the erosion:
  # plt.imshow(erosion, cmap = 'gray', interpolation = 'bicubic')
  # plt.xticks([]), plt.yticks([])
  # plt.show()


  # In[33]:


  ## Also it could be saved in file
  #im = Image.fromarray(erosion)
  #im.save("boundary_MASK.png")


  # **Quantification:**

  # In[34]:


  boundary_mask = erosion
  boundary_mask[boundary_mask >= 1] = 1


  # In[35]:


  # plt.imshow(boundary_mask, cmap = 'gray', interpolation = 'bicubic')
  # plt.xticks([]), plt.yticks([])
  # plt.show()


  # In[36]:


  ## label the boundary_mask
  labeled_array, num_features = label(boundary_mask)


  # In[37]:


  # num_features
  # #how many ROI/unconnected object it find
  #
  #
  # # In[38]:
  #
  #
  # plt.imshow(labeled_array)
  # #different colors conrespond to different labels (numbers)
  #
  #
  # # In[39]:
  #
  #
  # labeled_array


  # In[40]:


  unique, counts = np.unique(labeled_array, return_counts=True)
  dict(zip(unique, counts))


  # In[41]:


  region_label =[]
  pixel_area =[]
  Pixel_SUM =[]

  for i in range(num_features+1):
    if np.count_nonzero(labeled_array == i)>100: #discard small ROI. Does this code work?
      region_label.append(i) #get the region label
      #print('region label =', i)

      a = np.count_nonzero(labeled_array == i) #count the area of label size
      pixel_area.append(a)
      #print('pixel area =',np.count_nonzero(labeled_array == i))

      b = ndimage.sum(image, labeled_array, index=[i])
      #count pixel value in original image with label [i] in labeled_array
      Pixel_SUM.append(b)
      #print('SUM of Pixel intensity =',ndimage.sum(image, labeled_array, index=[i]))

      roi = deepcopy(labeled_array)

      roi[roi == i] = i
      roi[roi >i] = 0
      roi[roi<i] = 0

  #   plt.figure()
  #   plt.imshow(roi)
  #   print('------')
  #
  #
  # # In[42]:
  #
  #
  # region_label
  #
  #
  # # In[43]:
  #
  #
  # pixel_area
  #
  #
  # # In[44]:
  #
  #
  # Pixel_SUM


  # **I suggest that the USER determine which ROI they will use, because they could easily select the one they need. So, now you can just work with it.
  # We could add the feature that the computer discard the non-closed boundary later.**

  # In[45]:


  ## Note: built in ImageJ pixel summary method works on original image (RGB)
  ## Thus, mathmatical function is not SUM gray-scale image value


  # **Coordinates:**

  # In[46]:


  #coords = np.column_stack(np.where(labeled_array == 2))


  # In[47]:


  #coords


  # In[48]:


  #coords.shape


  # In[49]:


  #mask = labeled_array == 2


  # In[50]:


  #plt.imshow(mask, cmap = 'gray')
  #plt.xticks([]), plt.yticks([])
  #plt.show()


  # In[51]:


  ## Save the coordinates data in list:

  Boundary_ROI = []

  for i in range(num_features+1):
    coord = np.column_stack(np.where(labeled_array == i))
    Boundary_ROI.append(coord)


  # In[52]:


  # Boundary_ROI
  ## Boundary_ROI is a list, containing the ROI coordinates
  ## Boundary_ROI[i] gives us the coordinates (array type) of ROI labeled "i"


  # In[53]:


  # check Boundary_ROI[0] shape
  # Boundary_ROI[0].shape
  #
  #
  # # In[54]:
  #
  #
  # # check Boundary_ROI[1] shape
  # Boundary_ROI[1].shape


  # In[55]:


  # Check Boundary_ROI
  #Boundary_ROI


  # In[56]:


  ## Show each ROI:
  # for i in range(num_features+1):
  # mask = labeled_array == i
  #
  # plt.figure()
  # plt.imshow(mask)


  # **Contours of the Boundary ROIs:**

  # In[57]:


  ## needed for later use
  def bad_use_of_numpy(img, coords):
    for i, coord in enumerate(coords):
        img[coord[1], coord[0]] = 255

    return img


  # **Get the coordinates of all ROI contours:**

  # In[58]:


  # Creat a list to save the coordiantes of the ROI(boundary) contours:
  ROI_Contours_boundary = []
  ## the order of this list corresponds to the label in the labeled_array (i.e. ROI label)


  # In[59]:


  ###New: to see the contours and its corresponding label
  feature_boundary_countours_number = []


  # In[60]:


  for i in range(num_features+1):
    contour_to_be_find = deepcopy(labeled_array)

    # select the ROI to be contoured:
    contour_to_be_find[contour_to_be_find >i] = 0
    contour_to_be_find[contour_to_be_find <i] = 0
    contour_to_be_find[contour_to_be_find ==i] = 255
    contour_to_be_find = contour_to_be_find.astype(np.uint8)

    # make the contours:
    contours, hierarchy = cv.findContours(contour_to_be_find, cv.RETR_TREE,cv.CHAIN_APPROX_NONE)

    for a in range(len(contours)):
      coords_2 = np.array(contours[a])
      coords_2= np.squeeze(coords_2, axis=1)
      feature_boundary_countours_number.append(i) ##New added
      ROI_Contours_boundary.append(coords_2)


  # In[61]:


  # feature_boundary_countours_number
  #
  #
  # # In[62]:
  #
  #
  # #check the shape
  # ROI_Contours_boundary[0].shape
  #
  #
  # # In[63]:
  #
  #
  # # check the shape
  # ROI_Contours_boundary[1].shape


  # In[64]:


  # check the length
  len(ROI_Contours_boundary)


  # In[65]:


  # plot the contours:
  for i in range(len(ROI_Contours_boundary)):
    img_2 = deepcopy(np.zeros((len(boundary_mask),len(boundary_mask))))

    contour_bound = bad_use_of_numpy(img_2, ROI_Contours_boundary[i])
    contour_bound = contour_bound.astype(np.uint8)

    # plt.figure()
    # plt.imshow(contour_bound)


  # New: make the contours with same label in one image:

  # In[66]:


  # make a list to
  lists_1 = [[] for _ in range(num_features+1)]


  # In[67]:


  for i in range (num_features+1):
    for I, j in enumerate(feature_boundary_countours_number):
      if j == i:
        lists_1[i].append(I)


  # In[68]:


  #get the contours image:
  countour_boundary_image = []

  for i in range(len(ROI_Contours_boundary)):
    img_2 = deepcopy(np.zeros((len(boundary_mask),len(boundary_mask))))

    contour_bound = bad_use_of_numpy(img_2, ROI_Contours_boundary[i])
    contour_bound = contour_bound.astype(np.uint8)

    countour_boundary_image.append(contour_bound)
    # plt.figure()
    # plt.imshow(contour_bound)


  # In[69]:


  # make the countour with same label in the same image
  countour_boundary_image_combined = [[] for _ in range(num_features+1)]


  # In[70]:


  for i in range (num_features+1):
    countour_boundary_image_combined[i]= np.zeros((len(boundary_mask),len(boundary_mask)))


  # **New Update March 22:**

  # In[71]:


  for i in range (num_features+1):
    for _ in range(len(lists_1[i])):
      countour_boundary_image_combined[i] += countour_boundary_image[lists_1[i][_]]

    countour_boundary_image_combined[i][countour_boundary_image_combined[i] > 1] = i+1
  ## NOTE: the label for contours are different to labels for ROI_boundary


  # In[72]:


  # THE first image has pixel value 1 as label;
  # THE second image has pixel value 2 as label;
  # The third image has pixel value 3 as label.
  # for i in range (num_features+1):
  # plt.figure()
  # plt.imshow(countour_boundary_image_combined[i])


  # Make all the contours in one image, with values as label
  #

  # In[73]:


  ## Make all the contours in one image
  all_contours_1 = []
  all_contours_1 = np.zeros((len(boundary_mask),len(boundary_mask)))


  # In[74]:


  for i in range (len(countour_boundary_image)):
    all_contours_1 += countour_boundary_image[i]
    all_contours_1[all_contours_1 > 1] = 255

    #plt.figure()
    #plt.imshow(all_contours_1)


  # In[75]:


  # plt white and black image
  #plt.imshow(all_contours_1, cmap='gray', vmin=0, vmax=1)
  #plt.imshow(all_contours_1) #value >= 1 is white color
  ## plot image in gray scale is not an elegant method, it would be better to plot
  ### value in different colors.


  # In[76]:


  #all_contours_1 = all_contours_1.astype(np.uint8)
  ## plot image using PIL
  #im = Image.fromarray(all_contours_1)
  #im


  # **Close the boundary for interior quantification:**

  # In[77]:


  ## Check boundary_mask
  #boundary_mask


  # In[78]:


  ## Plot boundary_mask
  #plt.imshow(boundary_mask, cmap = 'gray', interpolation = 'bicubic')
  #plt.xticks([]), plt.yticks([])
  #plt.show()


  # In[79]:


  new_boundary_mask = deepcopy(boundary_mask)


  # In[80]:


  # Add value to the image margin
  ROW= [0,1,2,3,4,5]
  for I in ROW:
    new_boundary_mask[I,:]=1
    new_boundary_mask[:,I]=1
    new_boundary_mask[-I,:]=1
    new_boundary_mask[:,-I]=1

  ## because of bad annotation, the boundary in image margin would be miss classified.


  # In[81]:


  # new_boundary_mask


  # In[82]:


  # plt.imshow(new_boundary_mask, cmap = 'gray', interpolation = 'bicubic')
  # plt.xticks([]), plt.yticks([])
  # plt.show()


  # In[83]:


  new_boundary_mask = new_boundary_mask.astype('float32')


  # In[84]:


  ## Convert pixel value to show the interior:

  new_boundary_mask[new_boundary_mask == 1] = 0.5
  new_boundary_mask[new_boundary_mask == 0] = 1
  new_boundary_mask[new_boundary_mask == 0.5] = 0


  # In[85]:


  interior_mask = new_boundary_mask
  #
  #
  # # In[86]:
  #
  #
  # interior_mask
  #
  #
  # # In[87]:
  #
  #
  # plt.imshow(interior_mask, cmap = 'gray', interpolation = 'bicubic')
  # plt.xticks([]), plt.yticks([])
  # plt.show()
  #
  #
  # # In[88]:
  #
  #
  interior_mask = interior_mask.astype(np.uint8)
  #
  #
  # # **Quantification:**
  #
  # # In[89]:
  #
  #
  labeled_array_2, num_features_2 = label(interior_mask)


  # In[90]:


  #num_features_2


  # In[91]:


  #labeled_array_2


  # In[92]:


  #plt.imshow(labeled_array_2)
  #different colors conrespond to different labels (numbers)


  # In[93]:


  region_label_2 =[]
  pixel_area_2 =[]
  Pixel_SUM_2 =[]

  for i in range(num_features_2+1):
    if np.count_nonzero(labeled_array_2 == i)>100: #discard small ROI
      region_label_2.append(i) #get the region label
      #print('region label =', i)

      a = np.count_nonzero(labeled_array_2 == i) #count the area of label size
      pixel_area_2.append(a)
      #print('pixel area =',np.count_nonzero(labeled_array_2 == i))

      b = ndimage.sum(image, labeled_array_2, index=[i])
      #count pixel value in original image with label [i] in labeled_array
      Pixel_SUM.append(b)
      #print('SUM of Pixel intensity =',ndimage.sum(image, labeled_array_2, index=[i]))

      roi = deepcopy(labeled_array_2)

      roi[roi == i] = i
      roi[roi >i] = 0
      roi[roi<i] = 0

      # plt.figure()
      # plt.imshow(roi)
      # print('------')


  # Coordinates:

  # In[94]:


  ## Save the coordinates data in list:

  Interior_ROI = []

  for i in range(num_features_2+1):
    coord = np.column_stack(np.where(labeled_array_2 == i))
    Interior_ROI.append(coord)


  # In[95]:


  # Interior_ROI[0].shape


  # **Get coordinates of All ROI_interior contours:**

  # **NOTE: Code below are changed**

  # In[96]:


  # creat a list to save the coordiantes of ROI(interior) contours:
  ROI_Contours_interior = []
  ## the order of this list corresponds to the label in the labeled_array_2 (i.e. ROI label)


  # In[97]:


  # labeled_array_2
  #
  #
  # # In[98]:
  #
  #
  # labeled_array_2.shape


  # New: Make the contours with same feature_label in one image:

  # In[99]:


  ###New: to see the contours and its corresponding label
  feature_interior_countours_number = []


  # In[100]:


  for i in range(num_features_2+1):
    contour_to_be_find = deepcopy(labeled_array_2)

    contour_to_be_find[contour_to_be_find >i] = 0
    contour_to_be_find[contour_to_be_find <i] = 0
    contour_to_be_find[contour_to_be_find ==i] = 255
    contour_to_be_find = contour_to_be_find.astype(np.uint8)

    contours, hierarchy = cv.findContours(contour_to_be_find, cv.RETR_TREE,cv.CHAIN_APPROX_NONE)

    for a in range(len(contours)):
      coords_3 = np.array(contours[a])
      coords_3= np.squeeze(coords_3, axis=1)
      #print ('feature label =',i,';''countour label=',a)
      feature_interior_countours_number.append(i) ## new added

      ROI_Contours_interior.append(coords_3)


  # In[101]:


  #feature_interior_countours_number
  # we could find that the contours with feature label 1 are seperated \
  ##(in the second and third position of the following link)


  # In[102]:


  #feature_interior_countours_number.index(2)


  # In[103]:


  #How to use 'enumerate':
  #for i, j in enumerate(feature_interior_countours_number):
  #    if j == 1:
  #        print(i)


  # In[104]:


  ## create a list with the shape for label features
  lists = [[] for _ in range(num_features_2+1)]


  # In[105]:


  #lists


  # In[106]:


  for i in range (num_features_2+1):
    for I, j in enumerate(feature_interior_countours_number):
      if j == i:
          #print(I)
          lists[i].append(I)


  # In[107]:


  #lists


  # In[108]:


  #len(lists[1])


  # In[109]:


  #for i in range(len(lists[1])):
  #  print (i)


  # In[110]:


  #for i in range(len(lists[1])):
  #  print (lists[1][i])


  # In[111]:


  #for i in range (num_features_2+1):
  #
  #  for _ in range(len(lists[i])):
  #    print(lists[i][_])
  #  print('---')


  # In[112]:


  #get the contours image:
  countour_interior_image = []

  for i in range(len(ROI_Contours_interior)):
    img_2 = deepcopy(np.zeros((len(boundary_mask),len(boundary_mask))))

    contour_interior = bad_use_of_numpy(img_2, ROI_Contours_interior[i])
    contour_interior = contour_interior.astype(np.uint8)

    countour_interior_image.append(contour_interior)
  #plt.figure()
  #plt.imshow(contour_interior)


  # In[113]:


  #countour_interior_image


  # In[114]:


  #countour_interior_image[0]


  # In[115]:


  #lists


  # In[116]:


  # make the countour with same label in the same image
  countour_interior_image_combined = [[] for _ in range(num_features_2+1)]


  # In[117]:


  #countour_interior_image_combined


  # In[118]:


  for i in range (num_features_2+1):
    countour_interior_image_combined[i]= np.zeros((len(boundary_mask),len(boundary_mask)))


  # In[119]:


  #for i in range (num_features_2+1):
  #print(i)


  # **New Update March 22:**

  # In[120]:


  for i in range (num_features_2+1):
    for _ in range(len(lists[i])):
      countour_interior_image_combined[i] += countour_interior_image[lists[i][_]]

    countour_interior_image_combined[i][countour_interior_image_combined[i] > 1] = i+1
  ## NOTE: the label for contours are different to labels for ROI_interior


  # In[121]:


  return countour_interior_image_combined


  # In[122]:


  #np.set_printoptions(threshold=sys.maxsize)


  # In[144]:


  # countour_interior_image_combined[3][7]
  #
  #
  # # In[124]:
  #
  #
  # #np.set_printoptions(threshold = False)
  #
  #
  # # In[125]:
  #
  #
  # #import pandas as pd
  # countour_interior_image_combined[4][0]
  #
  #
  # # In[126]:
  #
  #
  # ## IMPORTANT: check what is the value of each label
  # #df = pd.DataFrame(countour_interior_image_combined[3])
  # #df.to_csv('file2.csv',index=False)
  #
  #
  # # In[127]:
  #
  #
  # # THE first image has pixel value 1 as label;
  # # THE second image has pixel value 2 as label;
  # # The third image has pixel value 3 as label.
  # for i in range (num_features_2+1):
  # plt.figure()
  # plt.imshow(countour_interior_image_combined[i])
  #
  #
  # # **Make all contours in one image, with value as labels:**
  #
  # # In[128]:
  #
  #
  # ## Make all the contours in one image
  # all_contours = []
  # all_contours = np.zeros((len(boundary_mask),len(boundary_mask)))
  #
  #
  # # In[129]:
  #
  #
  # for i in range (len(countour_interior_image_combined)):
  # all_contours += countour_interior_image_combined[i]
  # #all_contours[all_contours > 1] = 255
  #
  #
  # # In[130]:
  #
  #
  # plt.figure()
  # plt.imshow(all_contours)
  #
  #
  # # In[131]:
  #
  #
  # all_contours.shape
  #
  #
  # # In[132]:
  #
  #
  # all_contours
  #
  #
  # # In[133]:
  #
  #
  # #countour_interior_image
  #
  #
  # # In[134]:
  #
  #
  # # plt white and black image
  # plt.imshow(all_contours, cmap='gray', vmin=0, vmax=1) #value >= 1 is white color
  # ## plot image in gray scale is not an elegant method, it would be better to plot
  # ### value in different colors.
  # plt.show()
  # #plt.savefig('all_contour.png') # this save method does not work
  #
  #
  # # https://stackoverflow.com/questions/9707676/defining-a-discrete-colormap-for-imshow-in-matplotlib
  #
  # # In[135]:
  #
  #
  # # plot image using PIL ## not works now
  # #im = Image.fromarray(all_contours)
  # #im
  #
  #
  # # In[136]:
  #
  #
  # #check the image
  # #im.save("all_contours.png")
  #
  #
  # # **Future Work:**
  #
  # # 1. assign the label with a color
  # # 2. automatically find correct and appropriate ROI

if __name__ == "__main__":
  pixel_array = run_model(sys.argv[1], sys.argv[2])
  print(pixel_array)
  dir_path = os.path.dirname(os.path.realpath(__file__))
  save_file = dir_path + "/" + "pixel_array_output_test.txt"
  #np.savetxt(save_file, pixel_array, fmt="%s")
  pixel_ndarray = np.array(pixel_array)
  with open(save_file, 'w') as output:
    for slice_2d in pixel_ndarray:
      np.savetxt(output, slice_2d, fmt="%s")
