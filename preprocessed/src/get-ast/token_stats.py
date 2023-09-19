"""token_stats.py
- demo && test: '../../demo/get-ast/count_tokens.py'
"""

import os
import json


def count_version_tokens(version_dir: str):
    if not os.path.exists(version_dir):
        raise ValueError(f"'{version_dir}' does not exist!")

    def filter_condition(filename):
        return ".token" in filename

    token_cnt = 0
    for root, dirs, files in os.walk(version_dir):
        token_files = list(filter(filter_condition, files))
        token_cnt += len(token_files)
    return token_cnt


def count_project_tokens(project_dir: str):
    if not os.path.exists(project_dir):
        print(f"'{project_dir}' does not exist. Passed")
        return

    countDict = {}
    versions = os.listdir(project_dir)
    for versionName in versions:
        version_dir = os.path.join(project_dir, versionName)
        countDict[versionName] = count_version_tokens(version_dir)
    return countDict


def count_projects_tokens(token_root: str):
    countDict = {}
    projects = os.listdir(token_root)
    for projectName in projects:
        project_dir = os.path.join(token_root, projectName)
        countDict[projectName] = count_project_tokens(project_dir)
    return countDict


def save_token_counts(countDict: dict, trg_token_count_file: str):
    filedir = "/".join(trg_token_count_file.split("/")[:-1])
    print(filedir)
    if not os.path.exists(filedir):
        os.makedirs(filedir)
    
    with open(trg_token_count_file, "w") as f:
        json.dump(countDict, f, indent=2)
        print(f"Token counts saved to `{trg_token_count_file}`.")
