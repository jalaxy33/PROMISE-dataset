import os
import javalang
import pytest

def filter_condition(token):
    not_condition = (
        # isinstance(token, javalang.tokenizer.Separator)
        # or isinstance(token, javalang.tokenizer.Operator)
        # or isinstance(token, javalang.tokenizer.String)
        # or issubclass(type(token), javalang.tokenizer.Literal)
    )
    return not not_condition


def get_file_tokens(srcfile: str):
    with open(srcfile, encoding="utf-8") as f:
        contents = f.read()
    tokens = javalang.tokenizer.tokenize(contents)  # return generator
    tokens = list(filter(filter_condition, tokens))
    return tokens


def save_file_tokens(trgfile: str, tokens: list):
    trgdir = "/".join(trgfile.split('/')[:-1])
    if not os.path.exists(trgdir):
        os.makedirs(trgdir)

    token_values = list(tok.value + "\n" for tok in tokens)
    with open(trgfile, "w") as f:
        f.writelines(token_values)


def process_file_tokens(srcfile: str, trgfile: str):
    tokens = get_file_tokens(srcfile)
    save_file_tokens(trgfile, tokens)


# =========== testing functions =============


@pytest.fixture
def srcfile():
    return "source-code.java"


@pytest.fixture
def trgfile(srcfile):
    filedir = "tmpdir"
    filename = srcfile + ".tokens"
    return os.path.join(filedir, filedir, filename)


@pytest.mark.parametrize("print_range", [3])
def test_get_file_tokens(srcfile, print_range):
    tokens = get_file_tokens(srcfile)
    if isinstance(print_range, int) and print_range > 0:
        # print first several tokens
        for i in range(print_range):
            tok = tokens[i]
            print(tok.value, type(tok))
    elif print_range is None:
        # print all tokens
        for tok in tokens:
            print(tok.value, type(tok))
    else:
        assert False, "invalid print_range, should be unsigned_int or None"
    print("\n")


def test_save_file_tokens(srcfile, trgfile):
    tokens = get_file_tokens(srcfile)
    save_file_tokens(trgfile, tokens)
    print(f"Saved tokens to '{trgfile}'.\n")


def test_process_file_tokens(srcfile, trgfile):
    process_file_tokens(srcfile, trgfile)
    print(f"Get tokens of {srcfile} and saved to {trgfile}")
