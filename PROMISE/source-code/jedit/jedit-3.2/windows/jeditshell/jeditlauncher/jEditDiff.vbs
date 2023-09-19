' VBScript to activate JDiffPlugin in WinCVS

dim objArgs, launcher
set objArgs = wscript.Arguments
set launcher = CreateObject("JEdit.JEditLauncher")
launcher.runDiff objArgs(0), objArgs(1)

