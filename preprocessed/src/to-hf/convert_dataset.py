"""covert_dataset.py
- demo: '../../demo/to-hf/convert_dataset.demo.ipynb'
"""
import os
import re
import json
import pandas as pd


def remove_java_comments(codedata: str) -> str:
    """remove comments in the java source code"""
    codedata = re.sub(r"/\*(.*?)\*/", "", codedata, flags=re.MULTILINE | re.DOTALL)
    codedata = re.sub(r"//.*", "", codedata)
    codedata = re.sub(r"\n\n", "\n", codedata)
    return codedata


def read_source_file(sourceRoot, projectName, versionName, filename, postfix=".java"):
    filepath = os.path.join(sourceRoot, projectName, versionName, filename + postfix)
    lines, exist = "", False
    if os.path.exists(filepath):
        exist = True
        try:
            with open(filepath, encoding="utf-8") as f:
                lines = f.read()
        except:
            pass
    return lines, exist


def process_version(label_df, prefixDict, sourceRoot, projectName, versionName):
    size = label_df.shape[0]
    dataList = ["" for _ in range(size)]
    existList = [False for _ in range(size)]
    for i in range(size):
        filename = label_df.iloc[i]["name1"]
        if len(prefixDict[projectName]) > 0:
            filename = "/".join(
                prefixDict[projectName].split(".") + filename.split(".")
            )
        else:
            filename = "/".join(filename.split("."))
        lines, exist = read_source_file(sourceRoot, projectName, versionName, filename)
        dataList[i] = remove_java_comments(lines)
        existList[i] = exist

    # insert data to the dataframe
    df = label_df.copy()
    df["data"] = dataList
    df["exist"] = existList

    # remove all absent data rows
    df = df[df["exist"]]
    # remove extra columns
    df = df.drop(columns=["name1", "exist"])
    return df


def process_project(
    projectName, prefixDict, sourceRoot, labelRoot, saveRoot, to_compress=True
):
    proj_labelRoot = os.path.join(labelRoot, projectName)

    versionNames = [
        filename.split(".csv")[0] for filename in os.listdir(proj_labelRoot)
    ]
    labelpaths = [
        os.path.join(proj_labelRoot, version + ".csv") for version in versionNames
    ]

    versionNum = len(versionNames)
    for i in range(versionNum):
        versionName = versionNames[i]
        label_df = pd.read_csv(labelpaths[i])
        df = process_version(label_df, prefixDict, sourceRoot, projectName, versionName)
        # save dataframe
        if to_compress:
            saveRoot_real = os.path.join(saveRoot, "compressed")
            savepath = os.path.join(saveRoot_real, versionName + ".csv.gz")
            compression = {"method": "gzip", "compresslevel": 1, "mtime": 1}
        else:
            saveRoot_real = os.path.join(saveRoot, "csv")
            savepath = os.path.join(saveRoot_real, versionName + ".csv")
            compression = None

        if not os.path.exists(saveRoot_real):
            os.makedirs(saveRoot_real)
        df.to_csv(savepath, index=False, compression=compression)

        print(f"processed {versionName}: size={df.shape[0]}")


def process_all_projects(sourceRoot, labelRoot, saveRoot, prefixPath, to_compress=True):
    projectNames = os.listdir(labelRoot)
    with open(prefixPath) as f:
        prefixDict = json.load(f)

    for projectName in projectNames:
        process_project(
            projectName, prefixDict, sourceRoot, labelRoot, saveRoot, to_compress
        )


def process_split(splitDict, prefixDict, sourceRoot, labelRoot, shuffle=True):
    dfList = []
    for projectName, versionNames in splitDict.items():
        proj_labelRoot = os.path.join(labelRoot, projectName)
        labelpaths = [
            os.path.join(proj_labelRoot, version + ".csv") for version in versionNames
        ]
        versionNum = len(versionNames)
        for i in range(versionNum):
            versionName = versionNames[i]
            label_df = pd.read_csv(labelpaths[i])
            df = process_version(
                label_df, prefixDict, sourceRoot, projectName, versionName
            )
            dfList.append(df)
    # merge dataframes
    split_df = pd.concat(dfList)
    # shuffle data
    if shuffle:
        split_df = split_df.sample(frac=1).reset_index(drop=True)
    return split_df


def process_train_test_splits(
    labelRoot, testSplitFile, prefixPath, saveRoot, to_compress=True, shuffle=True
):
    # load split dicts
    with open(testSplitFile) as f:
        testSplitDict = json.load(f)
    testSplitDict = {k: [v] for k, v in testSplitDict.items()}

    projectNames = os.listdir(labelRoot)
    trainSplitDict = {}
    for projectName in projectNames:
        versionNames = [
            filename.split(".csv")[0]
            for filename in os.listdir(os.path.join(labelRoot, projectName))
        ]
        trainSplitDict[projectName] = [
            version
            for version in versionNames
            if version not in testSplitDict[projectName]
        ]

    # get split dataframes
    with open(prefixPath) as f:
        prefixDict = json.load(f)
    train_split_df = process_split(
        trainSplitDict, prefixDict, sourceRoot, labelRoot, shuffle
    )
    test_split_df = process_split(
        testSplitDict, prefixDict, sourceRoot, labelRoot, shuffle
    )

    # save dataframes
    if to_compress:
        saveRoot_real = os.path.join(saveRoot, "compressed")
        savepath_train = os.path.join(saveRoot_real, "train-split.csv.gz")
        savepath_test = os.path.join(saveRoot_real, "test-split.csv.gz")
        compression = {"method": "gzip", "compresslevel": 1, "mtime": 1}
    else:
        saveRoot_real = os.path.join(saveRoot, "csv")
        savepath_train = os.path.join(saveRoot_real, "train-split.csv")
        savepath_test = os.path.join(saveRoot_real, "test-split.csv")
        compression = None

    if not os.path.exists(saveRoot_real):
        os.makedirs(saveRoot_real)
    train_split_df.to_csv(savepath_train, index=False, compression=compression)
    test_split_df.to_csv(savepath_test, index=False, compression=compression)
    print(
        f"processed: size(train-split)={train_split_df.shape[0]}, size(test-split)={test_split_df.shape[0]}"
    )


if __name__ == "__main__":
    dataRoot = os.path.abspath("../../../PROMISE/")
    sourceRoot = os.path.join(dataRoot, "source-code")
    labelRoot = os.path.join(dataRoot, "labeled-data")
    prefixPath = os.path.join(dataRoot, "resources", "code-prefix.json")
    testSplitFile = os.path.join(dataRoot, 'resources', 'test-split.json')

    saveRoot = os.path.abspath("../../../promise-dataset-hf/")
    saveRoot_projs = os.path.join(saveRoot, "projects")
    saveRoot_splits = os.path.join(saveRoot, "splits")

    to_compress = True
    shuffle_splits = True

    print("\n>> Processing by projects >>\n")
    process_all_projects(sourceRoot, labelRoot, saveRoot_projs, prefixPath, to_compress)

    print("\n>> Processing by splits >>\n")
    process_train_test_splits(labelRoot, testSplitFile, prefixPath, saveRoot_splits, to_compress, shuffle=shuffle_splits)
