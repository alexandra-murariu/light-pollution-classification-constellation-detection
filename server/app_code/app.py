import io
import os
import numpy as np
from PIL import Image
from flask import Flask, request, jsonify
#from tensorflow.keras.utils import img_to_array
#from tensorflow.keras.applications.vgg16 import VGG16, preprocess_input
#import xgboost as xgb
#import pickle
from ultralytics import YOLO
import json

app = Flask(__name__)

#vgg16_model = VGG16(weights='imagenet', include_top=False)
# xgb_clf = xgb.Booster({'nthread': 4})
# xgb_clf.load_model('path/to/your/xgboost/model')
# with open('trained_pipeline.pkl', 'rb') as f:
#     xgb_clf = pickle.load(f)

yolo_model = YOLO('best.pt')

# def preprocess_image(image_bytes):
#     img = Image.open(io.BytesIO(image_bytes)).resize((224, 224))
#     x = img_to_array(img)
#     x = np.expand_dims(x, axis=0)
#     x = preprocess_input(x)
#     return x
#
#
# def extract_features(model, image_bytes):
#     x = preprocess_image(image_bytes)
#     features = model.predict(x)
#     return features.flatten()


@app.route('/predict', methods=['POST'])
def predict():
    print("Received request")
    if request.method == 'POST':
        print("Processing POST request with YOLO model")
        # file = request.files['file']
        # img_bytes = file.read()
        # image_features = extract_features(vgg16_model, img_bytes)
        # image_features = image_features.reshape(1, -1)
        # predicted_class = xgb_clf.predict(image_features)
        # return jsonify({'class_id': int(predicted_class[0])})
        file = request.files['file']
        img = Image.open(file)
        #results = yolo_model(img)
        
        # Extract the top predicted class label
        top1_class_id = yolo_model(img)[0].probs.cpu().numpy().top1

        # Construct the JSON response
        response = {
            'class_id': int(top1_class_id)
        }

        # Return the JSON response
        return json.dumps(response)


if __name__ == '__main__':
    app.run()
