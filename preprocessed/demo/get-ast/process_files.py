import os
import re
import json
import pandas as pd
import multiprocessing as mp
import pytest


def scan_projects(proj_data_root: str):
    if not os.path.exists(proj_data_root):
        raise ValueError(f"{proj_data_root} not exists!")

    projects = os.listdir(proj_data_root)
    projects.sort()
    projectsDict = dict()
    for project in projects:
        version_dir = os.path.join(proj_data_root, project)
        if len(versionList := os.listdir(version_dir)):
            sort_promise_versions(project, versionList)
            projectsDict[project] = versionList
        else:
            print(f"{version_dir} is empty.")
    return projectsDict


def sort_promise_versions(projectName: str, versionList: list):
    if len(versionList) == 0:
        return versionList

    def sort_xerces(projectName: str, versionList: list):
        if projectName != "xerces":
            raise ValueError(
                f"project name should be xerces, but {projectName} instead!"
            )

        versionList.sort()
        for i, version in enumerate(versionList):
            matchObj = re.search(r"init", version)
            if matchObj:
                versionList.pop(i)
                versionList.insert(0, version)
        return versionList

    if projectName == "xerces":  # special sort rule for `PROMISE/xerces` project
        versionList = sort_xerces(projectName, versionList)
    else:
        versionList.sort()
    return versionList


def process_version(
    projectName: str,
    versionName: str,
    proj_data_root: str,
    proj_label_root: str,
    projectsDict: dict,
    prefixDict: dict,
    proc_func=None,
    **proc_kwargs,
):
    if projectName not in projectsDict.keys():
        raise ValueError(f"Project {projectName} not exists!")
    if versionName not in projectsDict[projectName]:
        raise ValueError(f"Version {versionName} not exists!")

    if projectName not in prefixDict.keys():
        print(f"Prefix for project {projectName} not exist. Pass.")
        return [], []

    prefix = prefixDict[projectName]
    version_prefix = os.path.join(projectName, versionName, *prefix.split("."))
    path_prefix = os.path.join(proj_data_root, version_prefix)
    if not os.path.exists(path_prefix):
        raise ValueError(f"{path_prefix} not exists!")

    label_file = os.path.join(proj_label_root, projectName, versionName + ".csv")
    if not os.path.exists(label_file):
        raise ValueError(
            f"Label file {label_file} for version {versionName} not exists!"
        )

    labelDf = pd.read_csv(label_file)
    filesList = labelDf["name1"].values
    valid_files, invalid_files = [], []
    for filename in filesList:
        filepath = os.path.join(path_prefix, *filename.split("."))
        filepath += ".java"
        if os.path.exists(filepath):
            valid_files.append(filepath)
            if proc_func is not None:
                proc_kwargs["srcfile"] = filepath
                proc_kwargs["prefix"] = os.path.join(
                    version_prefix, *filename.split(".")[:-1]
                )
                try:
                    proc_func(**proc_kwargs)  # for user-defined function
                except TypeError:
                    proc_func(proc_kwargs)  # for print
                except:
                    print(f"Error processing `{proc_kwargs['srcfile']}`! Passed.")
        else:
            invalid_files.append(filepath)

    return valid_files, invalid_files


def process_project(
    projectName: str,
    proj_data_root: str,
    proj_label_root: str,
    projectsDict: dict,
    prefixDict: dict,
    use_mp=True,
    proc_func=None,
    **proc_kwargs,
):
    if projectName not in projectsDict.keys():
        raise ValueError(f"Project {projectName} not exists!")

    if projectName not in prefixDict.keys():
        print(f"Prefix for project {projectName} not exist. Pass.")
        return

    proj_data_dir = os.path.join(proj_data_root, projectName)
    versions = os.listdir(proj_data_dir)

    args = (
        proj_data_root,
        proj_label_root,
        projectsDict,
        prefixDict,
        proc_func,
    )
    if use_mp:
        process = [
            mp.Process(
                target=process_version,
                args=(projectName, versionName, *args),
                kwargs=proc_kwargs,
            )
            for versionName in versions
        ]
        [p.start() for p in process]
        [p.join() for p in process]
    else:
        for versionName in versions:
            process_version(projectName, versionName, *args, **proc_kwargs)


def process_projects(
    projects,
    proj_data_root: str,
    proj_label_root: str,
    projectsDict: dict,
    prefixDict: dict,
    use_mp=True,
    proc_func=None,
    **proc_kwargs,
):
    args = (
        proj_data_root,
        proj_label_root,
        projectsDict,
        prefixDict,
        use_mp,
        proc_func,
    )
    if use_mp:
        process = [
            mp.Process(
                target=process_project,
                args=(projectName, *args),
                kwargs=proc_kwargs,
            )
            for projectName in projects
        ]
        [p.start() for p in process]
        [p.join() for p in process]
    else:
        for projectName in projects:
            process_project(projectName, *args, **proc_kwargs)


# ========= test functions ==========


@pytest.fixture
def dataroot():
    return os.path.abspath("../../../PROMISE/")


@pytest.fixture
def proj_data_root(dataroot):
    return os.path.join(dataroot, "source-code")


@pytest.fixture
def proj_label_root(dataroot):
    return os.path.join(dataroot, "labeled-data")


@pytest.fixture
def prefix_info_file(dataroot):
    return os.path.join(dataroot, "resources/code-prefix.json")


def test_scan_projects(proj_data_root):
    projectsDict = scan_projects(proj_data_root)
    print(projectsDict)
    assert len(projectsDict) == 13


@pytest.mark.parametrize(
    "projectName, versionName, proc_func, proc_kwargs",
    [
        ("ant", "ant-1.3", None, {}),
        ("xalan", "xalan-2.4", None, {}),
        ("xalan", "xalan-2.6", None, {}),
    ],
)
def test_process_version(
    projectName,
    versionName,
    proj_data_root,
    proj_label_root,
    prefix_info_file,
    proc_func,
    proc_kwargs,
):
    with open(prefix_info_file, "r") as f:
        prefixDict = json.load(f)
    projectsDict = scan_projects(proj_data_root)

    valid_files, invalid_files = process_version(
        projectName,
        versionName,
        proj_data_root,
        proj_label_root,
        projectsDict,
        prefixDict,
        proc_func,
        **proc_kwargs,
    )
    print(len(valid_files), len(invalid_files))
    assert len(valid_files) > 0


@pytest.mark.parametrize(
    "projectName, use_mp, proc_func, proc_kwargs",
    [("ant", False, None, {}), ("ivy", True, None, {}), ("xalan", True, None, {})],
)
def test_process_project(
    projectName,
    proj_data_root,
    proj_label_root,
    prefix_info_file,
    use_mp,
    proc_func,
    proc_kwargs,
):
    with open(prefix_info_file, "r") as f:
        prefixDict = json.load(f)
    projectsDict = scan_projects(proj_data_root)

    process_project(
        projectName,
        proj_data_root,
        proj_label_root,
        projectsDict,
        prefixDict,
        use_mp,
        proc_func,
        **proc_kwargs,
    )


@pytest.mark.parametrize(
    "projects, use_mp, proc_func, proc_kwargs",
    [(["ant", "ivy", "xalan"], True, print, {})],
)
def test_process_projects(
    projects,
    proj_data_root,
    proj_label_root,
    prefix_info_file,
    use_mp,
    proc_func,
    proc_kwargs,
):
    with open(prefix_info_file, "r") as f:
        prefixDict = json.load(f)
    projectsDict = scan_projects(proj_data_root)

    process_projects(
        projects,
        proj_data_root,
        proj_label_root,
        projectsDict,
        prefixDict,
        use_mp,
        proc_func,
        **proc_kwargs,
    )
