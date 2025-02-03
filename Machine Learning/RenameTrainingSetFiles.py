import os

dataset_in = "Training data sets"

def rename_images(main_folder):
    counter = 1

    for root, _, files in os.walk(main_folder):
        files.sort()

        for filename in files:
            file_path = os.path.join(root, filename)

            # Check if the path is a file
            if os.path.isfile(file_path):
                # Create the new filename 
                new_filename = f"{counter:08d}"

                # Get extension
                _, file_extension = os.path.splitext(filename)


                new_file_path = os.path.join(root, new_filename + file_extension)
                os.rename(file_path, new_file_path)
                print(f"Renamed: {file_path} -> {new_file_path}")
                counter += 1

rename_images(dataset_in)
