/*
 * P4.java
 *
 * Copyright (C) 1998-2004 Peter Graves
 * $Id: P4.java,v 1.14 2004-08-08 00:54:04 piso Exp $
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.armedbear.j;

import gnu.regexp.RE;
import gnu.regexp.REMatch;
import gnu.regexp.UncheckedRE;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.undo.CompoundEdit;

public class P4 implements Constants
{
    public static void p4()
    {
        if (!checkP4Installed())
            return;
        MessageDialog.showMessageDialog("The command \"p4\" requires an argument.",
                                        "Error");
    }

    public static void p4(String s)
    {
        if (!checkP4Installed())
            return;
        List args = Utilities.tokenize(s);
        if (args.size() == 0)
            return;
        String command = (String) args.get(0);
        if (command.equals("submit")) {
            MessageDialog.showMessageDialog("Use \"p4Submit\".", "Error");
            return;
        }
        if (command.equals("change")) {
            MessageDialog.showMessageDialog("Use \"p4Change\".", "Error");
            return;
        }
        final Editor editor = Editor.currentEditor();
        editor.setWaitCursor();
        FastStringBuffer sb = new FastStringBuffer("p4 ");
        for (Iterator it = args.iterator(); it.hasNext();) {
            String arg = (String) it.next();
            if (arg.equals("%")) {
                File file = editor.getBuffer().getFile();
                if (file != null)
                    arg = file.canonicalPath();
            }
            sb.append(maybeQuote(arg));
            sb.append(' ');
        }
        final String cmd = sb.toString().trim();
        final Buffer parentBuffer = editor.getBuffer();
        Runnable commandRunnable = new Runnable() {
            public void run()
            {
                final String output =
                    command(cmd, editor.getCurrentDirectory());
                Runnable completionRunnable = new Runnable() {
                    public void run()
                    {
                        p4Completed(editor, parentBuffer, cmd, output);
                    }
                };
                SwingUtilities.invokeLater(completionRunnable);
            }
        };
        new Thread(commandRunnable).start();
    }

    private static void p4Completed(Editor editor, Buffer parentBuffer,
        String cmd, String output)
    {
        if (output != null && output.length() > 0) {
            Buffer buf;
            if (cmd.startsWith("p4 diff"))
                buf = new DiffOutputBuffer(parentBuffer, output, VC_P4);
            else
                buf = OutputBuffer.getOutputBuffer(output);
            buf.setTitle(cmd);
            editor.makeNext(buf);
            editor.activateInOtherWindow(buf);
        }
    }

    public static void add()
    {
        if (!checkP4Installed())
            return;
        final Editor editor = Editor.currentEditor();
        final Buffer buffer = editor.getBuffer();
        if (buffer.getFile() == null)
            return;
        editor.setWaitCursor();
        final String name = buffer.getFile().getName();
        FastStringBuffer sb = new FastStringBuffer("p4 add ");
        sb.append(maybeQuote(name));
        final String cmd = sb.toString();
        final String output = command(cmd, buffer.getCurrentDirectory());
        OutputBuffer buf = OutputBuffer.getOutputBuffer(output);
        buf.setTitle(cmd);
        editor.makeNext(buf);
        editor.activateInOtherWindow(buf);
        editor.setDefaultCursor();
    }

    public static void edit()
    {
        if (!checkP4Installed())
            return;
        final Editor editor = Editor.currentEditor();
        final Buffer buffer = editor.getBuffer();
        final File file = buffer.getFile();
        if (file == null)
            return;
        buffer.setBusy(true);
        editor.setWaitCursor();
        FastStringBuffer sb = new FastStringBuffer("p4 edit ");
        sb.append(maybeQuote(file.canonicalPath()));
        final String cmd = sb.toString();
        Runnable commandRunnable = new Runnable() {
            public void run()
            {
                final String output = command(cmd, null);
                Runnable completionRunnable = new Runnable() {
                    public void run()
                    {
                        editCompleted(editor, buffer, cmd, output);
                    }
                };
                SwingUtilities.invokeLater(completionRunnable);
            }
        };
        new Thread(commandRunnable).start();
    }

    private static void editCompleted(Editor editor, Buffer buffer,
        String cmd, String output)
    {
        // Don't bother with output buffer unless there's an error.
        if (output.trim().endsWith(" - opened for edit")) {
            editor.status("File opened for edit");
        } else {
            OutputBuffer buf = OutputBuffer.getOutputBuffer(output);
            buf.setTitle(cmd);
            editor.makeNext(buf);
            editor.activateInOtherWindow(buf);
        }
        // Update read-only status.
        if (editor.reactivate(buffer))
            Sidebar.repaintBufferListInAllFrames();
        buffer.setBusy(false);
        EditorIterator iter = new EditorIterator();
        while (iter.hasNext()) {
            Editor ed = iter.nextEditor();
            if (ed.getBuffer() == buffer)
                ed.setDefaultCursor();
        }
    }

    // For Editor.checkReadOnly(). Displays output buffer if necessary.
    public static boolean autoEdit(Editor editor)
    {
        if (editor == null)
            return false;
        final Buffer buffer = editor.getBuffer();
        final File file = buffer.getFile();
        String output = _autoEdit(file);
        if (output == null)
            return false;
        FastStringBuffer sb = new FastStringBuffer("p4 edit ");
        sb.append(maybeQuote(file.canonicalPath()));
        editCompleted(editor, buffer, sb.toString(), output);
        return !buffer.isReadOnly();
    }

    // For replaceInFiles(). Returns false if there are any complications.
    public static boolean autoEdit(File file)
    {
        final String output = _autoEdit(file);
        if (output == null)
            return false;
        return output.trim().endsWith(" - opened for edit");
    }

    // Returns output from "p4 edit" command or null if error.
    private static String _autoEdit(File file)
    {
        if (file == null)
            return null;
        if (file.isRemote())
            return null;
        if (!haveP4())
            return null;
        FastStringBuffer sb = new FastStringBuffer("p4 edit ");
        sb.append(maybeQuote(file.canonicalPath()));
        return command(sb.toString(), null);
    }

    public static void revert()
    {
        if (!checkP4Installed())
            return;
        final Editor editor = Editor.currentEditor();
        final Buffer buffer = editor.getBuffer();
        final File file = buffer.getFile();
        if (file == null)
            return;
        if (buffer.isModified()) {
            String prompt =
                "Discard changes to " + maybeQuote(file.getName()) + "?";
            if (!editor.confirm("Revert Buffer", prompt))
                return;
        }
        final String cmd = "p4 revert " + maybeQuote(file.canonicalPath());
        String output = command(cmd, null);
        if (output.trim().endsWith(" - was edit, reverted")) {
            editor.status("File reverted");
        } else {
            OutputBuffer buf = OutputBuffer.getOutputBuffer(output);
            buf.setTitle(cmd);
            editor.makeNext(buf);
            editor.activateInOtherWindow(buf);
        }
        editor.reload(buffer);
        // Update read-only status.
        if (editor.reactivate(buffer))
            Sidebar.repaintBufferListInAllFrames();
    }

    public static void diff()
    {
        if (!checkP4Installed())
            return;
        final Editor editor = Editor.currentEditor();
        Buffer parentBuffer = editor.getBuffer();
        if (parentBuffer instanceof CheckinBuffer)
            parentBuffer = parentBuffer.getParentBuffer();
        final File file = parentBuffer.getFile();
        if (file == null)
            return;
        final String baseCmd = "p4 diff -du ";
        final String name = file.getName();
        final String title = baseCmd + maybeQuote(name);
        boolean save = false;
        if (parentBuffer.isModified()) {
            int response =  ConfirmDialog.showConfirmDialogWithCancelButton(
                editor, CHECK_SAVE_PROMPT, "P4 diff");
            switch (response) {
                case RESPONSE_YES:
                    save = true;
                    break;
                case RESPONSE_NO:
                    break;
                case RESPONSE_CANCEL:
                    return;
            }
            editor.repaintNow();
        }
        editor.setWaitCursor();
        if (!save || parentBuffer.save()) {
            // Kill existing diff output buffer if any for same parent buffer.
            for (BufferIterator it = new BufferIterator(); it.hasNext();) {
                Buffer b = it.nextBuffer();
                if (b instanceof DiffOutputBuffer) {
                    if (b.getParentBuffer() == parentBuffer) {
                        editor.maybeKillBuffer(b);
                        break; // There should be one at most.
                    }
                }
            }
            final String cmd = baseCmd + maybeQuote(file.canonicalPath());
            final String output = command(cmd, null);
            DiffOutputBuffer buf = new DiffOutputBuffer(parentBuffer, output, VC_P4);
            buf.setTitle(title);
            Editor otherEditor = editor.getOtherEditor();
            if (otherEditor != null) {
                buf.setUnsplitOnClose(otherEditor.getBuffer().unsplitOnClose());
                otherEditor.makeNext(buf);
            } else
                buf.setUnsplitOnClose(true);
            editor.activateInOtherWindow(buf);
            editor.setDefaultCursor();
        }
    }

    public static void diffDir()
    {
        if (!checkP4Installed())
            return;
        final Editor editor = Editor.currentEditor();
        final Buffer buffer = editor.getBuffer();
        editor.setWaitCursor();
        final String cmd = "p4 diff -du";
        final File directory = buffer.getCurrentDirectory();
        // Kill existing diff output buffer if any for same directory.
        for (BufferIterator it = new BufferIterator(); it.hasNext();) {
            Buffer b = it.nextBuffer();
            if (b instanceof DiffOutputBuffer) {
                if (directory.equals(((DiffOutputBuffer) b).getDirectory())) {
                    editor.maybeKillBuffer(b);
                    break; // There should be one at most.
                }
            }
        }
        final String output = command(cmd, directory);
        DiffOutputBuffer buf = new DiffOutputBuffer(directory, output, VC_P4);
        if (buf != null) {
            buf.setTitle(cmd);
            editor.makeNext(buf);
            editor.activateInOtherWindow(buf);
        }
        editor.setDefaultCursor();
    }

    public static void log()
    {
        log(null);
    }

    public static void log(String args)
    {
        boolean useCurrentFile = true;
        if (args == null)
            args = "-l";
        List list = Utilities.tokenize(args);
        for (int i = 0; i < list.size(); i++) {
            String arg = (String) list.get(i);
            if (arg.charAt(0) != '-') {
                // Must be a filename.
                useCurrentFile = false;
                break;
            }
        }
        final Editor editor = Editor.currentEditor();
        final Buffer parentBuffer = editor.getBuffer();
        FastStringBuffer sb = new FastStringBuffer("p4 filelog ");
        sb.append(args);
        if (useCurrentFile) {
            if (parentBuffer.getFile() == null)
                return;
            final String name = parentBuffer.getFile().getName();
            sb.append(' ');
            if (name.indexOf(' ') >= 0) {
                // Enclose filename in double quotes since it contains an
                // embedded space.
                sb.append('"');
                sb.append(name);
                sb.append('"');
            } else
                sb.append(name);
        }
        final String cmd = sb.toString();
        editor.setWaitCursor();
        final String output = command(cmd, parentBuffer.getCurrentDirectory());
        OutputBuffer buf = OutputBuffer.getOutputBuffer(output);
        buf.setTitle(cmd);
        editor.makeNext(buf);
        editor.activateInOtherWindow(buf);
        editor.setDefaultCursor();
    }

    public static void change(String arg)
    {
        arg = arg.trim();
        try {
            // Make sure arg is a number.
            Integer.parseInt(arg);
            _change(arg);
        }
        catch (NumberFormatException e) {
            MessageDialog.showMessageDialog(
                "Argument must be a changelist number.",
                "Error");
        }
    }

    public static void change()
    {
        _change(null);
    }

    // arg must be a changelist number or null.
    private static void _change(String arg)
    {
        if (!checkP4Installed())
            return;
        final Editor editor = Editor.currentEditor();
        Buffer parentBuffer = editor.getBuffer();
        if (parentBuffer instanceof DiffOutputBuffer) {
            Log.debug("parentBuffer is DiffOutputBuffer");
            parentBuffer = parentBuffer.getParentBuffer();
            Log.debug("==> parentBuffer is " + parentBuffer);
        }
        if (parentBuffer == null)
            return;
        if (parentBuffer.getFile() == null)
            return;
        FastStringBuffer sb = new FastStringBuffer("p4 change");
        if (arg != null) {
            sb.append(' ');
            sb.append(arg);
        }
        final String title = sb.toString();
        CheckinBuffer checkinBuffer = null;
        for (BufferIterator it = new BufferIterator(); it.hasNext();) {
            Buffer buf = it.nextBuffer();
            if (buf instanceof CheckinBuffer) {
                if (title.equals(buf.getTitle())) {
                    checkinBuffer = (CheckinBuffer) buf;
                    break;
                }
            }
        }
        if (checkinBuffer == null) {
            checkinBuffer = new CheckinBuffer(parentBuffer, VC_P4, true);
            checkinBuffer.setProperty(Property.USE_TABS, true);
            checkinBuffer.setFormatter(new P4ChangelistFormatter(checkinBuffer));
            checkinBuffer.setTitle(title);
            sb.setText("p4 change -o");
            if (arg != null) {
                sb.append(' ');
                sb.append(arg);
            }
            ShellCommand shellCommand = new ShellCommand(sb.toString());
            shellCommand.run();
            checkinBuffer.setText(shellCommand.getOutput());
            Position dot = findStartOfComment(checkinBuffer);
            if (dot != null) {
                Position mark = findEndOfComment(checkinBuffer, dot);
                View view = new View();
                view.setDot(dot);
                view.setCaretCol(checkinBuffer.getCol(dot));
                if (mark != null)
                    view.setMark(mark);
                checkinBuffer.setLastView(view);
            }
        }
        editor.makeNext(checkinBuffer);
        editor.activateInOtherWindow(checkinBuffer);
    }

    public static void submit(String args)
    {
        String message = null;
        List list = Utilities.tokenize(args);
        if (list.size() == 2) {
            String arg = (String) list.get(0);
            if (arg.equals("-c")) {
                arg = (String) list.get(1);
                try {
                    Integer.parseInt(arg);
                    // Success!
                    _submit(arg);
                    return;
                }
                catch (NumberFormatException e) {
                    message = "Invalid changelist number";
                }
            }
        }
        if (message == null) {
            FastStringBuffer sb =
                new FastStringBuffer("Unrecognized argument \"");
            sb.append(args.trim());
            sb.append('"');
            message = sb.toString();
        }
        MessageDialog.showMessageDialog(message, "Error");
    }

    public static void submit()
    {
        _submit(null);
    }

    // arg must be a changelist number or null.
    private static void _submit(String arg)
    {
        if (!checkP4Installed())
            return;
        final Editor editor = Editor.currentEditor();
        Buffer parentBuffer = editor.getBuffer();
        if (parentBuffer instanceof DiffOutputBuffer)
            parentBuffer = parentBuffer.getParentBuffer();
        FastStringBuffer sb = new FastStringBuffer("p4 submit");
        if (arg != null) {
            sb.append(" -c ");
            sb.append(arg);
        }
        final String title = sb.toString();
        boolean save = false;
        List list = getModifiedBuffers();
        if (list != null && list.size() > 0) {
            int response =
                ConfirmDialog.showConfirmDialogWithCancelButton(editor,
                    "Save modified buffers first?", title);
            switch (response) {
                case RESPONSE_YES:
                    save = true;
                    break;
                case RESPONSE_NO:
                    break;
                case RESPONSE_CANCEL:
                    return;
            }
            editor.repaintNow();
        }
        if (!save || saveModifiedBuffers(editor, list)) {
            // Look for existing checkin buffer before making a new one.
            CheckinBuffer checkinBuffer = null;
            for (BufferIterator it = new BufferIterator(); it.hasNext();) {
                Buffer buf = it.nextBuffer();
                if (buf instanceof CheckinBuffer) {
                    if (title.equals(buf.getTitle())) {
                        checkinBuffer = (CheckinBuffer) buf;
                        break;
                    }
                }
            }
            if (checkinBuffer == null) {
                checkinBuffer = new CheckinBuffer(parentBuffer, VC_P4);
                checkinBuffer.setProperty(Property.USE_TABS, true);
                checkinBuffer.setFormatter(new P4ChangelistFormatter(checkinBuffer));
                checkinBuffer.setTitle(title);
                sb.setText("p4 change -o");
                if (arg != null) {
                    sb.append(' ');
                    sb.append(arg);
                }
                ShellCommand shellCommand = new ShellCommand(sb.toString());
                shellCommand.run();
                checkinBuffer.setText(shellCommand.getOutput());
                Position dot = findStartOfComment(checkinBuffer);
                if (dot != null) {
                    Position mark = findEndOfComment(checkinBuffer, dot);
                    View view = new View();
                    view.setDot(dot);
                    view.setCaretCol(checkinBuffer.getCol(dot));
                    if (mark != null)
                        view.setMark(mark);
                    checkinBuffer.setLastView(view);
                }
            }
            editor.makeNext(checkinBuffer);
            editor.activate(checkinBuffer);
        }
    }

    private static List getModifiedBuffers()
    {
        ArrayList list = null;
        for (BufferIterator it = new BufferIterator(); it.hasNext();) {
            Buffer buf = it.nextBuffer();
            if (!buf.isModified())
                continue;
            if (buf.isUntitled())
                continue;
            final int modeId = buf.getModeId();
            if (modeId == SEND_MAIL_MODE)
                continue;
            if (modeId == CHECKIN_MODE)
                continue;
            if (buf.getFile() != null && buf.getFile().isLocal()) {
                if (list == null)
                    list = new ArrayList();
                list.add(buf);
            }
        }
        return list;
    }

    private static boolean saveModifiedBuffers(Editor editor, List list)
    {
        editor.setWaitCursor();
        int numErrors = 0;
        for (Iterator it = list.iterator(); it.hasNext();) {
            Buffer buf = (Buffer) it.next();
            if (buf.getFile() != null && buf.getFile().isLocal()) {
                editor.status("Saving modified buffers...");
                if (buf.getBooleanProperty(Property.REMOVE_TRAILING_WHITESPACE))
                    buf.removeTrailingWhitespace();
                if (!buf.save())
                    ++numErrors;
            }
        }
        editor.setDefaultCursor();
        if (numErrors == 0) {
            editor.status("Saving modified buffers...done");
            return true;
        }
        // User will already have seen detailed error information from Buffer.save().
        editor.status("");
        return false;
    }

    public static void replaceComment(final Editor editor, final String comment)
    {
        if (!(editor.getBuffer() instanceof CheckinBuffer)) {
            Debug.bug();
            return;
        }
        final CheckinBuffer buffer = (CheckinBuffer) editor.getBuffer();
        String oldComment = extractComment(buffer);
        if (oldComment.equals(comment))
            return;
        insertComment(editor, comment);
    }

    public static String extractComment(final CheckinBuffer buffer)
    {
        Position begin = findStartOfComment(buffer);
        if (begin != null) {
            Position end = findEndOfComment(buffer, begin);
            if (end != null) {
                int offset1 = buffer.getAbsoluteOffset(begin);
                int offset2 = buffer.getAbsoluteOffset(end);
                if (offset1 >= 0 && offset2 > offset1) {
                    String s = buffer.getText().substring(offset1, offset2);
                    if (!s.equals("<enter description here>"))
                        return s;
                }
            }
        }
        return "";
    }

    private static void insertComment(final Editor editor, final String comment)
    {
        final CheckinBuffer buffer = (CheckinBuffer) editor.getBuffer();
        Position dot = findStartOfComment(buffer);
        if (dot == null)
            return;
        Position mark = findEndOfComment(buffer, dot);
        if (mark == null)
            return;
        try {
            buffer.lockWrite();
        }
        catch (InterruptedException e) {
            Log.error(e);
            return;
        }
        try {
            CompoundEdit compoundEdit = editor.beginCompoundEdit();
            editor.moveDotTo(dot);
            editor.setMark(mark);
            editor.deleteRegion();
            editor.insertString(comment);
            editor.endCompoundEdit(compoundEdit);
            buffer.modified();
        }
        finally {
            buffer.unlockWrite();
        }
        final Position end = findEndOfComment(buffer, null);
        for (EditorIterator it = new EditorIterator(); it.hasNext();) {
            Editor ed = it.nextEditor();
            if (ed.getBuffer() == buffer) {
                ed.setTopLine(buffer.getFirstLine());
                ed.setDot(end.copy()); // No undo.
                ed.moveCaretToDotCol();
                ed.setUpdateFlag(REPAINT);
                ed.updateDisplay();
            }
        }
    }

    private static Position findStartOfComment(CheckinBuffer buffer)
    {
        String s = buffer.getText();
        String lookFor = "\nDescription:\n\t";
        RE re = new UncheckedRE(lookFor);
        REMatch match = re.getMatch(s);
        if (match != null)
            return buffer.getPosition(match.getStartIndex() + lookFor.length());
        return null;
    }

    private static Position findEndOfComment(CheckinBuffer buffer, Position start)
    {
        String s = buffer.getText();
        String lookFor = "\n\nFiles:\n\t";
        RE re = new UncheckedRE(lookFor);
        int offset = -1;
        if (start != null)
            offset = buffer.getAbsoluteOffset(start);
        if (offset < 0)
            offset = 0;
        REMatch match = re.getMatch(s, offset);
        if (match != null)
            return buffer.getPosition(match.getStartIndex());
        return null;
    }

    public static void finish(Editor editor, CheckinBuffer checkinBuffer)
    {
        final Buffer parentBuffer = checkinBuffer.getParentBuffer();
        editor.getFrame().setWaitCursor();
        final boolean editOnly = checkinBuffer.isEditOnly();
        final String cmd;
        final String title;
        if (editOnly) {
            cmd = "p4 change -i";
            title = "Output from p4 change";
        } else {
            cmd = "p4 submit -i";
            title = "Output from p4 submit";
        }
        final String input = checkinBuffer.getText();
        ShellCommand shellCommand = new ShellCommand(cmd, null, input);
        shellCommand.run();
        if (shellCommand.exitValue() != 0) {
            // Error.
            Log.error("P4.finish input = |" + input + "|");
            Log.error("P4.finish exit value = " + shellCommand.exitValue());
            OutputBuffer buf = null;
            // Re-use existing output buffer if possible.
            for (BufferIterator it = new BufferIterator(); it.hasNext();) {
                Buffer b = it.nextBuffer();
                if (b instanceof OutputBuffer) {
                    if (title.equals(b.getTitle())) {
                        buf = (OutputBuffer) b;
                        break; // There should be one at most.
                    }
                }
            }
            if (buf != null)
                buf.setText(shellCommand.getOutput());
            else
                buf = OutputBuffer.getOutputBuffer(shellCommand.getOutput());
            buf.setTitle(title);
            editor.makeNext(buf);
            editor.displayInOtherWindow(buf);
        } else {
            // Success. Kill old diff and output buffers, if any: their
            // contents are no longer correct.
            if (!editOnly && parentBuffer != null) {
                for (BufferIterator it = new BufferIterator(); it.hasNext();) {
                    Buffer b = it.nextBuffer();
                    if (b instanceof DiffOutputBuffer) {
                        if (b.getParentBuffer() == parentBuffer) {
                            Debug.assertTrue(Editor.getBufferList().contains(b));
                            Log.debug("P4.finish killing diff output buffer");
                            b.kill();
                            Debug.assertFalse(Editor.getBufferList().contains(b));
                            Debug.assertTrue(editor.getBuffer() != b);
                            Editor otherEditor = editor.getOtherEditor();
                            if (otherEditor != null)
                                Debug.assertTrue(otherEditor.getBuffer() != b);
                            break; // There should be one at most.
                        }
                    }
                }
            }
            for (BufferIterator it = new BufferIterator(); it.hasNext();) {
                Buffer b = it.nextBuffer();
                if (b instanceof OutputBuffer) {
                    if (title.equals(b.getTitle())) {
                        editor.maybeKillBuffer(b);
                        break; // One at most.
                    }
                }
            }
            if (!editOnly)
                // Read-only status of some buffers may have changed.
                editor.getFrame().reactivate();
            editor.otherWindow();
            editor.unsplitWindow();
            checkinBuffer.kill();
        }
        editor.getFrame().setDefaultCursor();
    }

    public static String getStatusString(File file)
    {
        if (file != null && haveP4()) {
            FastStringBuffer sb = null;
            String output =
                command("p4 fstat ".concat(file.canonicalPath()), null);
            String HAVE_REV = "... haveRev ";
            int begin = output.indexOf(HAVE_REV);
            if (begin >= 0) {
                begin += HAVE_REV.length();
                int end = output.indexOf('\n', begin);
                if (end > begin) {
                    if (sb == null)
                        sb = new FastStringBuffer("Perforce");
                    sb.append(" revision ");
                    sb.append(output.substring(begin, end).trim());
                }
            }
            String ACTION = "... action ";
            begin = output.indexOf(ACTION);
            if (begin >= 0) {
                begin += ACTION.length();
                int end = output.indexOf('\n', begin);
                if (end > begin) {
                    if (sb == null)
                        sb = new FastStringBuffer("Perforce");
                    sb.append(" (opened for ");
                    sb.append(output.substring(begin, end).trim());
                    sb.append(')');
                }
            }
            if (sb != null)
                return sb.toString();
        }
        return null;
    }

    // Implementation.
    private static final String command(String cmd, File workingDirectory)
    {
        ShellCommand shellCommand = new ShellCommand(cmd, workingDirectory);
        shellCommand.run();
        return shellCommand.getOutput();
    }

    private static boolean checkP4Installed()
    {
        if (haveP4())
            return true;
        MessageDialog.showMessageDialog(
            "The Perforce command-line client does not appear to be in your PATH.",
            "Error");
        return false;
    }

    private static int haveP4 = -1;

    private static boolean haveP4()
    {
        if (haveP4 > 0)
            return true;
        if (Utilities.have("p4")) {
            haveP4 = 1; // Cache positive result.
            return true;
        }
        return false;
    }

    // Enclose string in quotes if it contains any embedded spaces.
    private static String maybeQuote(String s)
    {
        if (s.indexOf(' ') < 0)
            return s;
        FastStringBuffer sb = new FastStringBuffer('"');
        sb.append(s);
        sb.append('"');
        return sb.toString();
    }
}
