import os
import json
import pytest
from process_files import scan_projects, process_projects
from get_ast import process_file_tokens


def token_proc_func(srcfile: str, trg_token_root: str, prefix: str, verbose=True):
    filename = os.path.join(srcfile.split("/")[-1])
    trgfile = os.path.join(trg_token_root, prefix, filename + ".token")
    process_file_tokens(srcfile, trgfile)
    if verbose:
        print(f"[Processed] '{srcfile}'. [Saved to] '{trgfile}'")


def gen_projects_tokens(
    projects,
    proj_data_root: str,
    proj_label_root: str,
    trg_token_root: str,
    projectsDict: dict,
    prefixDict: dict,
    use_mp=True,
    verbose=True,
):
    proc_func = token_proc_func
    proc_kwargs = dict(trg_token_root=trg_token_root, verbose=verbose)
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
    print("\n")


def get_all_projects(proj_data_root: str):
    return os.listdir(proj_data_root)


# ============== testing ====================


@pytest.fixture
def trg_token_root():
    return "token-data"


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


@pytest.mark.parametrize(
    "projects, use_mp, verbose", [(["ant", "ivy", "xalan"], True, True)]
)
def test_gen_projects_tokens(
    projects,
    proj_data_root,
    proj_label_root,
    prefix_info_file,
    trg_token_root,
    use_mp,
    verbose,
):
    with open(prefix_info_file, "r") as f:
        prefixDict = json.load(f)
    projectsDict = scan_projects(proj_data_root)

    gen_projects_tokens(
        projects,
        proj_data_root,
        proj_label_root,
        trg_token_root,
        projectsDict,
        prefixDict,
        use_mp,
        verbose,
    )


def test_get_all_projects(proj_data_root):
    all_projects = get_all_projects(proj_data_root)
    print(all_projects)
    assert len(all_projects) == 13
