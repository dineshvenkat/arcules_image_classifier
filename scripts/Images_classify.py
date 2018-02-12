

"""

This program uses a supervised classification approach to return
a list of URLs of daytime images, and a list of URL of nighttime images,
stored in a Google Cloud storage bucket. 

Usage:  python findSampleImages_classify.py

"""

import numpy as np
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split
from sklearn.svm import SVC
from PIL import Image
import pickle
import urllib
import urllib2
import shutil
import os
import sys



def day_night_image_classify(local_url, model, feat_scaler):
    """

    This function uses the previously obtained model and transformations
    to perform day/night scene classification of repository images.
    The repository images do not need to be stored in a local directory.

    Args:
        local_url: The URL of the image file 
        model: The classification model
        feat_scaler: The feature normalization transformation to be applied to
                each normalized histogram.

    Returns:
        Day or Night

    """
    raw = Image.open(local_url)
    if len(np.array(raw).shape) == 3:
        # Extract R, G, B components, and obtain grayscale
        # pixel intensity from the components
        red = np.array(raw)[:, :, 0]
        grn = np.array(raw)[:, :, 1]
        blu = np.array(raw)[:, :, 2]
        grayscale = 0.2126 * red + 0.7152 * grn + 0.0722 * blu
    else:
        # Image was grayscale to begin with
        grayscale = np.array(raw)

    im_flat = grayscale.reshape(1, grayscale.shape[0]*grayscale.shape[1])

    # Extract pixels for top third of image
    im_flat_topthird = im_flat[0][0:int(len(im_flat[0])/3)]

    # Generate histogram of the image pixel intensities
    im_hist = np.histogram(im_flat_topthird, bins=20, range=(0, 255))[0]

    feats_test = im_hist/float(np.sum(im_hist))
    feats_test = feat_scaler.transform(feats_test.reshape(1, -1))

    # Generate predicted class of test image
    yhat = model.predict(feats_test)
    if yhat == 1:
        result = "day"
    else:
        result = "night"


    return result


def load_models():
    """

    This function loads the classification model and feature scaling
    transformations from the local disk. This allows the user to run this
    program without having to re-train the model and transformations
    each time (if the same model and transformations are to be used).

    Args:
        None

    Returns:
        model: The classification model
        feat_scaler: The feature-scaling transformation

    """

    # Load model and transformations to disk (if needed)
    model = pickle.load(open('bin/saved_svm_model', 'rb'))
    #model = pickle.load(open('scripts/saved_svm_model', 'rb'))
    feat_scaler = pickle.load(open('bin/saved_sc', 'rb'))
    #feat_scaler = pickle.load(open('scripts/saved_sc', 'rb'))

    return model, feat_scaler


def main():
    
    
    filePath = sys.argv[1]
    # Load model and transformations to disk (if needed)
    model, feat_scaler = load_models()

    # Use trained model to classify images in repository
    res = day_night_image_classify(filePath,
                                                  model, feat_scaler)
    print(res)
   

if __name__ == '__main__':
    main()
