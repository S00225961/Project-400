import os
import random
import shutil

dataset_in = "Training data sets"
dataset_out = dataset_in + '_split'

def split_into_train_test(images_origin, images_dest, test_split):
    """Splits a directory of images into training and test sets, recursively searching for files."""
    # Walk through all subdirectories and collect image paths
    class_to_files = {}
    for root, dirs, files in os.walk(images_origin):
        # Skip the top-level folder
        if root == images_origin:
            continue

        # Check if the current directory contains image files
        valid_files = [
            os.path.join(root, f) for f in files if (
                f.endswith('.png') or f.endswith('.jpg') or f.endswith('.jpeg') or f.endswith('.bmp')
            )
        ]
        if valid_files:
            # Use the folder name relative to the top-level folder as the class label
            class_label = os.path.relpath(root, images_origin)  
            class_to_files.setdefault(class_label, []).extend(valid_files)

    # Create train and test directories
    TRAIN_DIR = os.path.join(images_dest, 'train')
    TEST_DIR = os.path.join(images_dest, 'test')
    os.makedirs(TRAIN_DIR, exist_ok=True)
    os.makedirs(TEST_DIR, exist_ok=True)

    for class_label, file_paths in class_to_files.items():
        # Skip classes without valid files
        if not file_paths:
            print(f"No valid images found for class '{class_label}'. Skipping...")
            continue

        # Shuffle the files deterministically
        file_paths.sort()
        random.seed(42)
        random.shuffle(file_paths)

        # Split into train and test sets
        test_count = int(len(file_paths) * test_split)
        class_train_dir = os.path.join(TRAIN_DIR, class_label)
        class_test_dir = os.path.join(TEST_DIR, class_label)
        os.makedirs(class_train_dir, exist_ok=True)
        os.makedirs(class_test_dir, exist_ok=True)

        for i, file_path in enumerate(file_paths):
            if i < test_count:
                destination = os.path.join(class_test_dir, os.path.basename(file_path))
            else:
                destination = os.path.join(class_train_dir, os.path.basename(file_path))
            shutil.copyfile(file_path, destination)

        print(f"Moved {test_count} of {len(file_paths)} images from class '{class_label}' into test set.")

    print(f"Your split dataset is saved in '{images_dest}'")

# Call the function
split_into_train_test(dataset_in, dataset_out, test_split=0.2)