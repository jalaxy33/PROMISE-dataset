"""main.py
- demo & test: '../../demo/get-ast/main.py'
"""

import os
import json
from project_utils import scan_projects, process_projects
from ast_utils import process_file_tokens
from token_stats import count_projects_tokens, save_token_counts


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


def main(use_mp=True, verbose=True):
    with open(prefix_info_file, "r") as f:
        prefixDict = json.load(f)
    projectsDict = scan_projects(proj_data_root)

    all_projects = os.listdir(proj_data_root)
    gen_projects_tokens(
        all_projects,
        proj_data_root,
        proj_label_root,
        trg_token_root,
        projectsDict,
        prefixDict,
        use_mp,
        verbose,
    )

    tokenCountDict = count_projects_tokens(trg_token_root)
    print(tokenCountDict)
    save_token_counts(tokenCountDict, trg_token_count_file)




if __name__ == "__main__":
    # source data
    dataroot = os.path.abspath("../../../PROMISE/")
    proj_data_root = os.path.join(dataroot, "source-code")
    proj_label_root = os.path.join(dataroot, "labeled-data")
    prefix_info_file = os.path.join(dataroot, "resources/code-prefix.json")

    # target token data
    trg_token_root = os.path.join(dataroot, "token-data")
    trg_token_count_file = os.path.join(dataroot, "resources/token-count.json")


    main(use_mp=True, verbose=False)
