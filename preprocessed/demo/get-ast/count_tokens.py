import os
import json
import pytest


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
        print(f"'{project_dir}' does not exist. Passed.")
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
    filedir = os.path.join(*trg_token_count_file.split("/")[:-1])
    if not os.path.exists(filedir):
        os.makedirs(filedir)

    with open(trg_token_count_file, "w") as f:
        json.dump(countDict, f, indent=2)
        print(f"Token counts of projects saved to `{trg_token_count_file}`.")


# ========== testing ===============


@pytest.fixture
def token_root():
    return "token-data"


@pytest.fixture
def trg_token_count_file():
    filename = "token-count.json"
    filedir = "tmpsave"
    return os.path.join(filedir, filename)


@pytest.mark.parametrize(
    "projectName, versionName",
    [
        ("ant", "ant-1.4"),
        ("ivy", "ivy-2.0"),
        ("xalan", "xalan-2.4"),
        ("xalan", "xalan-2.6"),
    ],
)
def test_count_version_token(token_root, projectName, versionName):
    version_dir = os.path.join(token_root, projectName, versionName)
    token_cnt = count_version_tokens(version_dir)
    print(token_cnt)
    assert token_cnt > 0


@pytest.mark.parametrize("projectName", [("ant"), ("ivy"), ("xalan")])
def test_count_project_tokens(token_root, projectName):
    project_dir = os.path.join(token_root, projectName)
    countDict = count_project_tokens(project_dir)
    print(countDict)


def test_count_projects_tokens(token_root):
    countDict = count_projects_tokens(token_root)
    print(countDict)


def test_save_token_counts(token_root, trg_token_count_file):
    countDict = count_projects_tokens(token_root)
    save_token_counts(countDict, trg_token_count_file)
