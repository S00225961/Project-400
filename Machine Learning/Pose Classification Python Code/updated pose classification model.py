import os
import tensorflow as tf
from tensorflow import keras
import cv2
import itertools
import numpy as np
import pandas as pd
import sys
import requests
import subprocess
from matplotlib import pyplot as plt
from matplotlib.collections import LineCollection
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix
import matplotlib.gridspec as gridspec
from sklearn.metrics import roc_curve, auc
from sklearn.preprocessing import label_binarize

# check if tensorflow is using gpu and if not change
print("Num GPUs Available: ", len(tf.config.experimental.list_physical_devices('GPU')))
tf.test.is_built_with_cuda()
print(tf.version.VERSION)
import sys
sys.version

# Detect available GPUs
gpus = tf.config.experimental.list_physical_devices('GPU')

if gpus:
    try:
        # Enable memory growth to prevent TensorFlow from consuming all GPU memory
        for gpu in gpus:
            tf.config.experimental.set_memory_growth(gpu, True)
        
        # Print detected GPUs
        logical_gpus = tf.config.experimental.list_logical_devices('GPU')
        print(f"Physical GPUs: {gpus}")
        print(f"Logical GPUs: {logical_gpus}")
        print("TensorFlow will now automatically use the GPU when available.")
    
    except RuntimeError as e:
        print(e)
else:
    print("No GPU found, using CPU.")

# Replace wget with requests to download the model
url = 'https://tfhub.dev/google/lite-model/movenet/singlepose/thunder/tflite/float16/4?lite-format=tflite'
response = requests.get(url)
with open('movenet_lightning.tflite', 'wb') as f:
    f.write(response.content)

# Replace git clone with subprocess to clone the repo
subprocess.run(['git', 'clone', 'https://github.com/tensorflow/examples.git'])
pose_sample_rpi_path = os.path.join(os.getcwd(), 'examples/lite/examples/pose_estimation/raspberry_pi')
sys.path.append(pose_sample_rpi_path)

# Load MoveNet Thunder model
import utils
from data import BodyPart
from ml import Movenet
movenet = Movenet('movenet_lightning')

# Define function to run pose estimation using MoveNet Thunder.
# You'll apply MoveNet's cropping algorithm and run inference multiple times on
# the input image to improve pose estimation accuracy.
def detect(input_tensor, inference_count=3):
    """Runs detection on an input image."""
    image_height, image_width, channel = input_tensor.shape
    # Detect pose using the full input image
    movenet.detect(input_tensor.numpy(), reset_crop_region=True)

    # Repeatedly using previous detection result to identify the region of
    # interest and only cropping that region to improve detection accuracy
    for _ in range(inference_count - 1):
        person = movenet.detect(input_tensor.numpy(), reset_crop_region=False)

    return person

is_skip_step_1 = False  # @param ["False", "True"] {type:"raw"}
use_custom_dataset = True  # @param ["False", "True"] {type:"raw"}
dataset_is_split = False  # @param ["False", "True"] {type:"raw"}
IMAGES_ROOT = "C:/Users/sh0ut/OneDrive/Desktop/Newt Scmander's Case/Uni 2024  2025/Project 400/Pose Classification Model/Training data sets_split"

if not is_skip_step_1 and not use_custom_dataset:
    url = "http://download.tensorflow.org/data/pose_classification/yoga_poses.zip"
    response = requests.get(url)
    with open("yoga_poses.zip", 'wb') as f:
        f.write(response.content)
    # Unzip the dataset
    import zipfile
    with zipfile.ZipFile('yoga_poses.zip', 'r') as zip_ref:
        zip_ref.extractall('yoga_cg')
    IMAGES_ROOT = "yoga_cg"

if not is_skip_step_1:
    images_in_train_folder = os.path.join(IMAGES_ROOT, 'train')
    images_out_train_folder = 'poses_images_out_train'
    csvs_out_train_path = os.path.join(os.path.dirname(__file__), 'train_data.csv')


    # preprocessor = MoveNetPreprocessor(
    #     images_in_folder=images_in_train_folder,
    #     images_out_folder=images_out_train_folder,
    #     csvs_out_path=csvs_out_train_path,
    # )
    # preprocessor.process(per_pose_class_limit=None)
if not is_skip_step_1:
    images_in_test_folder = os.path.join(IMAGES_ROOT, 'test')
    images_out_test_folder = 'poses_images_out_test'
    csvs_out_test_path = os.path.join(os.path.dirname(__file__), 'test_data.csv')


    # preprocessor = MoveNetPreprocessor(
    #     images_in_folder=images_in_test_folder,
    #     images_out_folder=images_out_test_folder,
    #     csvs_out_path=csvs_out_test_path,
    # )
    # preprocessor.process(per_pose_class_limit=None)

# Download the preprocessed CSV files which are the same as the output of step 1
if is_skip_step_1:
    url_train = "http://download.tensorflow.org/data/pose_classification/yoga_train_data.csv"
    url_test = "http://download.tensorflow.org/data/pose_classification/yoga_test_data.csv"

    response_train = requests.get(url_train)
    with open('train_data.csv', 'wb') as f:
        f.write(response_train.content)

    response_test = requests.get(url_test)
    with open('test_data.csv', 'wb') as f:
        f.write(response_test.content)

    csvs_out_train_path = os.path.join(os.path.dirname(__file__), 'train_data.csv')
    csvs_out_test_path = os.path.join(os.path.dirname(__file__), 'test_data.csv')
    is_skipped_step_1 = True

def load_pose_landmarks(csv_path):
    """Loads a CSV created by MoveNetPreprocessor."""
    dataframe = pd.read_csv(csv_path)
    df_to_process = dataframe.copy()

    df_to_process.drop(columns=['file_name'], inplace=True)

    classes = df_to_process.pop('class_name').unique()
    y = df_to_process.pop('class_no')

    X = df_to_process.astype('float64')
    y = keras.utils.to_categorical(y)

    return X, y, classes, dataframe

# Load the train data
csvs_out_train_path = os.path.join(os.path.dirname(__file__), 'train_data.csv')
csvs_out_test_path = os.path.join(os.path.dirname(__file__), 'test_data.csv')
X, y, class_names, _ = load_pose_landmarks(csvs_out_train_path)

# Split training data (X, y) into (X_train, y_train) and (X_val, y_val)
X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.15)
# Load the test data
X_test, y_test, _, df_test = load_pose_landmarks(csvs_out_test_path)

def get_center_point(landmarks, left_bodypart, right_bodypart):
    """Calculates the center point of the two given landmarks."""
    left = tf.gather(landmarks, left_bodypart.value, axis=1)
    right = tf.gather(landmarks, right_bodypart.value, axis=1)
    center = left * 0.5 + right * 0.5
    return center

def get_pose_size(landmarks, torso_size_multiplier=2.5):
    """Calculates pose size."""
    hips_center = get_center_point(landmarks, BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP)
    shoulders_center = get_center_point(landmarks, BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER)

    torso_size = tf.linalg.norm(shoulders_center - hips_center)
    pose_center_new = get_center_point(landmarks, BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP)
    pose_center_new = tf.expand_dims(pose_center_new, axis=1)
    pose_center_new = tf.broadcast_to(pose_center_new, [tf.size(landmarks) // (17*2), 17, 2])

    d = tf.gather(landmarks - pose_center_new, 0, axis=0, name="dist_to_pose_center")
    max_dist = tf.reduce_max(tf.linalg.norm(d, axis=0))

    pose_size = tf.maximum(torso_size * torso_size_multiplier, max_dist)
    return pose_size

class NormalizePoseLandmarks(tf.keras.layers.Layer):
    def call(self, landmarks):
        pose_center = get_center_point(landmarks, BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP)
        pose_center = tf.expand_dims(pose_center, axis=1)
        pose_center = tf.broadcast_to(pose_center, tf.shape(landmarks))
        landmarks = landmarks - pose_center

        pose_size = get_pose_size(landmarks)
        landmarks /= pose_size

        return landmarks

class LandmarksToEmbedding(tf.keras.layers.Layer):
    def call(self, landmarks_and_scores):
        reshaped_inputs = tf.reshape(landmarks_and_scores, (-1, 17, 3))
        landmarks = NormalizePoseLandmarks()(reshaped_inputs[:, :, :2])
        embedding = tf.keras.layers.Flatten()(landmarks)
        return embedding

# Define the model
inputs = tf.keras.Input(shape=(51,))  # 17 landmarks × 3 dimensions
embedding = LandmarksToEmbedding()(inputs)

layer = keras.layers.Dense(512, activation='relu')(embedding)
layer = keras.layers.BatchNormalization()(layer)
layer = keras.layers.Dropout(0.3)(layer)

layer = keras.layers.Dense(256, activation='relu')(layer)
layer = keras.layers.BatchNormalization()(layer)
layer = keras.layers.Dropout(0.4)(layer)

layer = keras.layers.Dense(128, activation='relu')(layer)
layer = keras.layers.BatchNormalization()(layer)
layer = keras.layers.Dropout(0.4)(layer)

layer = keras.layers.Dense(64, activation='relu')(layer)
layer = keras.layers.BatchNormalization()(layer)
layer = keras.layers.Dropout(0.4)(layer)

residual = keras.layers.Dense(64, activation='relu')(embedding)
layer = keras.layers.Add()([layer, residual])

outputs = keras.layers.Dense(len(class_names), activation="softmax")(layer)

model = keras.Model(inputs, outputs)
model.summary()

model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

checkpoint_path = "weights.best.keras"
checkpoint = keras.callbacks.ModelCheckpoint(checkpoint_path, monitor='val_accuracy', verbose=1, save_best_only=True, mode='max')
earlystopping = keras.callbacks.EarlyStopping(monitor='val_accuracy', patience=20)

history = model.fit(X_train, y_train, epochs=150, batch_size=32, validation_data=(X_val, y_val), callbacks=[checkpoint, earlystopping])

plt.plot(history.history['accuracy'])
plt.plot(history.history['val_accuracy'])
plt.title('Model accuracy')
plt.ylabel('accuracy')
plt.xlabel('epoch')
plt.legend(['TRAIN', 'VAL'], loc='lower right')
plt.show()

loss, accuracy = model.evaluate(X_test, y_test)

def plot_confusion_matrix(cm, classes, normalize=False, title='Confusion matrix', cmap=plt.cm.Blues):
    plt.figure(figsize=(14, 12))  # Wider and taller figure

    if normalize:
        cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]
        print("Normalized confusion matrix")
    else:
        print('Confusion matrix, without normalization')

    plt.imshow(cm, interpolation='nearest', cmap=cmap)
    plt.title(title, fontsize=16)

    tick_marks = np.arange(len(classes))
    plt.xticks(tick_marks, classes, rotation=45, ha="right", fontsize=9)
    plt.yticks(tick_marks, classes, fontsize=9)

    fmt = '.2f' if normalize else 'd'
    thresh = cm.max() / 2.

    for i, j in itertools.product(range(cm.shape[0]), range(cm.shape[1])):
        plt.text(j, i, format(cm[i, j], fmt),
                 ha="center", va="center",
                 color="white" if cm[i, j] > thresh else "black",
                 fontsize=7)

    plt.ylabel('True label', fontsize=12)
    plt.xlabel('Predicted label', fontsize=12)
    plt.tight_layout()
    plt.show()

def plot_multiclass_roc(y_true, y_score, class_names, figsize=(10, 8)):
    """Plots the ROC Curve for multi-class classification"""
    n_classes = len(class_names)
    # Compute ROC curve and ROC area for each class
    fpr = dict()
    tpr = dict()
    roc_auc = dict()

    for i in range(n_classes):
        fpr[i], tpr[i], _ = roc_curve(y_true[:, i], y_score[:, i])
        roc_auc[i] = auc(fpr[i], tpr[i])

    # Compute micro-average ROC curve and ROC area
    fpr["micro"], tpr["micro"], _ = roc_curve(y_true.ravel(), y_score.ravel())
    roc_auc["micro"] = auc(fpr["micro"], tpr["micro"])

    # Plot all ROC curves
    plt.figure(figsize=figsize)
    plt.plot(fpr["micro"], tpr["micro"],
             label='micro-average ROC (area = {0:0.2f})'
                   ''.format(roc_auc["micro"]),
             color='deeppink', linestyle=':', linewidth=4)

    colors = plt.cm.get_cmap('tab10', n_classes)

    for i, color in zip(range(n_classes), colors.colors):
        plt.plot(fpr[i], tpr[i], color=color, lw=2,
                 label='ROC curve of class {0} (area = {1:0.2f})'
                 ''.format(class_names[i], roc_auc[i]))

    plt.plot([0, 1], [0, 1], 'k--', lw=2)
    plt.xlim([-0.05, 1.05])
    plt.ylim([-0.05, 1.05])
    plt.xlabel('False Positive Rate')
    plt.ylabel('True Positive Rate')
    plt.title('ROC Curve for Multi-class Pose Classification')
    plt.legend(loc="lower right", fontsize='small')
    plt.grid(alpha=0.3)
    plt.tight_layout()
    plt.show()

y_pred = model.predict(X_test)

y_pred_label = [class_names[i] for i in np.argmax(y_pred, axis=1)]
y_true_label = [class_names[i] for i in np.argmax(y_test, axis=1)]

cm = confusion_matrix(np.argmax(y_test, axis=1), np.argmax(y_pred, axis=1))
plot_confusion_matrix(cm, class_names, title='Confusion Matrix of Pose Classification Model')
plot_multiclass_roc(y_test, y_pred, class_names)

print('\nClassification Report:\n', classification_report(y_true_label, y_pred_label))

# Save TFLite model and evaluate its accuracy
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model = converter.convert()

print(f'Model size: {len(tflite_model) / 1024:.2f} KB')

with open('pose_classifier.tflite', 'wb') as f:
    f.write(tflite_model)

with open('pose_labels.txt', 'w') as f:
    f.write('\n'.join(class_names))

# Evaluate the TFLite model accuracy
def evaluate_model(interpreter, X, y_true):
    input_index = interpreter.get_input_details()[0]["index"]
    output_index = interpreter.get_output_details()[0]["index"]
    y_pred = []
    for i in range(len(y_true)):
        test_image = X[i: i + 1].astype('float32')
        interpreter.set_tensor(input_index, test_image)
        interpreter.invoke()
        output = interpreter.tensor(output_index)
        predicted_label = np.argmax(output()[0])
        y_pred.append(predicted_label)
    y_pred = keras.utils.to_categorical(y_pred)
    return accuracy_score(y_true, y_pred)

classifier_interpreter = tf.lite.Interpreter(model_content=tflite_model)
classifier_interpreter.allocate_tensors()
print(f'Accuracy of TFLite model: {evaluate_model(classifier_interpreter, X_test, y_test)}')
