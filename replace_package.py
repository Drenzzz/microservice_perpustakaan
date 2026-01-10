import os

root_dir = '.'
search_text = 'com.pail'
replace_text = 'com.naufal'
extensions = ['.java', '.xml', '.yml', '.yaml', '.properties', '.md', '.txt']
ignore_dirs = ['target', '.git', '.idea', '.vscode']

print(f"Starting replacement: '{search_text}' -> '{replace_text}'")

count = 0
for subdir, dirs, files in os.walk(root_dir):
    # Filter directories
    dirs[:] = [d for d in dirs if d not in ignore_dirs]
    
    for file in files:
        if any(file.endswith(ext) for ext in extensions) or file == 'Jenkinsfile' or file == 'Dockerfile':
            filepath = os.path.join(subdir, file)
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                if search_text in content:
                    new_content = content.replace(search_text, replace_text)
                    with open(filepath, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                    print(f"Updated: {filepath}")
                    count += 1
            except Exception as e:
                print(f"Error processing {filepath}: {e}")

print(f"Replacement complete. Updated {count} files.")
