/*
 * Property.java
 *
 * Copyright (C) 2000-2002 Peter Graves
 * $Id: Property.java,v 1.2 2002-09-25 13:31:42 piso Exp $
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

import java.util.Hashtable;

public final class Property implements Comparable, Constants
{
    private static final Hashtable ht = new Hashtable();

    // Integer properties.
    public static final Property ADJUST_ASCENT =
        createProperty("adjustAscent", 0);
    public static final Property ADJUST_DESCENT =
        createProperty("adjustDescent", 0);
    public static final Property ADJUST_LEADING =
        createProperty("adjustLeading", 0);
    public static final Property CHANGE_MARK_WIDTH =
        createProperty("changeMarkWidth", 4);
    public static final Property CHECK_IDLE_BACKGROUND =
        createProperty("checkIdleBackground", 10);
    public static final Property CHECK_IDLE_FOREGROUND =
        createProperty("checkIdleForeground", 60);
    public static final Property CHECK_INTERVAL =
        createProperty("checkInterval", 60);
    public static final Property DIALOG_FONT_SIZE =
        createProperty("dialogFontSize", 11);
    public static final Property FONT_SIZE =
        createProperty("fontSize", 12);
    public static final Property GUTTER_FONT_SIZE =
        createProperty("gutterFontSize", 0);
    public static final Property INDENT_SIZE =
        createProperty("indentSize", 4);
    public static final Property JLIST_FIXED_CELL_HEIGHT =
        createProperty("JList.fixedCellHeight", 0);
    public static final Property LOG_MAX_BACKUP_INDEX =
        createProperty("Log.maxBackupIndex", 0);
    public static final Property LOG_MAX_FILE_SIZE =
        createProperty("Log.maxFileSize", 100000);
    public static final Property PRINTER_FONT_SIZE =
        createProperty("printerFontSize", 10);
    public static final Property SHELL_OUTPUT_LIMIT =
        createProperty("shellOutputLimit", 1000);
    public static final Property SSH_TIMEOUT =
        createProperty("sshTimeout", 250);
    public static final Property STATUS_BAR_DISPLAY_CONTEXT =
        createProperty("StatusBar.displayContext", 1);
    public static final Property TAB_WIDTH =
        createProperty("tabWidth", 8);
    public static final Property TEXT_FIELD_FONT_SIZE =
        createProperty("textFieldFontSize", 0);
    public static final Property VERTICAL_RULE =
        createProperty("verticalRule", 0);
    public static final Property VERTICAL_SCROLL_INCREMENT =
        createProperty("verticalScrollIncrement", 5);
    public static final Property WRAP_COL =
        createProperty("wrapCol", 80);

    // Boolean properties.
    public static final Property ANTIALIAS =
        createProperty("antialias", false);
    public static final Property ATTRIBUTES_REQUIRE_QUOTES =
        createProperty("attributesRequireQuotes", true);
    public static final Property AUTOSAVE_NAMED_SESSIONS =
        createProperty("autosaveNamedSessions", false);
    public static final Property AUTO_INDENT =
        createProperty("autoIndent", true);
    public static final Property AUTO_NEWLINE =
        createProperty("autoNewline", false);
    public static final Property AUTO_SELECT_LINE =
        createProperty("autoSelectLine", true);
    public static final Property BEAUTIFY_HEADERS =
        createProperty("beautifyHeaders", true);
    public static final Property CHECK_ENABLED =
        createProperty("checkEnabled", false);
    public static final Property CONFIRM_SEND =
        createProperty("confirmSend", false);
    public static final Property DEBUG =
        createProperty("debug", false);
    public static final Property DIR_SORT_DIRECTORIES_FIRST =
        createProperty("dirSortDirectoriesFirst", true);
    public static final Property DIR_USE_NATIVE_FORMAT =
        createProperty("dirUseNativeFormat", false);
    public static final Property EMULATE_BOLD =
        createProperty("emulateBold", false);
    public static final Property ENABLE_CACHE =
        createProperty("enableCache", false);
    public static final Property ENABLE_DRAG_TEXT =
        createProperty("enableDragText", true);
    public static final Property ENABLE_EXPERIMENTAL_FEATURES =
        createProperty("enableExperimentalFeatures", false);
    public static final Property ENABLE_ITALICS =
        createProperty("enableItalics", true);
    public static final Property ENABLE_MAIL =
        createProperty("enableMail", false);
    public static final Property ENABLE_TOOL_TIPS =
        createProperty("enableToolTips", false);
    public static final Property ENABLE_TREE =
        createProperty("enableTree", true);
    public static final Property ENABLE_WEB =
        createProperty("enableWeb", false);
    public static final Property EXTEND_END =
        createProperty("extendEnd", false);
    public static final Property EXTEND_HOME =
        createProperty("extendHome", false);
    public static final Property FILENAME_COMPLETIONS_IGNORE_CASE =
        createProperty("filenameCompletionsIgnoreCase", true);
    public static final Property FIX_CASE =
        createProperty("fixCase", false);
    public static final Property FTP_USE_PASSIVE_MODE =
        createProperty("ftpUsePassiveMode", true);
    public static final Property GROUP_BY_THREAD =
        createProperty("groupByThread", false);
    public static final Property HIGHLIGHT_BRACKETS =
        createProperty("highlightBrackets", false);
    public static final Property HIGHLIGHT_MATCHING_BRACKET =
        createProperty("highlightMatchingBracket", false);
    public static final Property HTTP_ENABLE_COOKIES =
        createProperty("httpEnableCookies", false);
    public static final Property IMAP_USE_LOCAL_CACHE =
        createProperty("imapUseLocalCache", true);
    public static final Property INDENT_AFTER_BRACE =
        createProperty("indentAfterBrace", true);
    public static final Property INDENT_AFTER_OPENING_BRACE =
        createProperty("indentAfterOpeningBrace", true);
    public static final Property INDENT_BEFORE_BRACE =
        createProperty("indentBeforeBrace", false);
    public static final Property INDENT_LINE_FIX_WHITESPACE =
        createProperty("indentLineFixWhitespace", false);
    public static final Property LINEUP_ARGLIST =
        createProperty("lineupArglist", true);
    public static final Property LOG_ENABLED =
        createProperty("Log.enabled", true);
    public static final Property OFFLINE =
        createProperty("offline", false);
    public static final Property P4_AUTO_EDIT =
        createProperty("p4AutoEdit", false);
    public static final Property PARENS_REQUIRE_SPACES =
        createProperty("parensRequireSpaces", false);
    public static final Property POP_EXPUNGE_DELETED_MESSAGES_ON_SERVER =
        createProperty("popExpungeDeletedMessagesOnServer", false);
    public static final Property POP_KEEP_MESSAGES_ON_SERVER =
        createProperty("popKeepMessagesOnServer", true);
    public static final Property REORDER_BUFFERS =
        createProperty("reorderBuffers", true);
    public static final Property REMOVE_TRAILING_WHITESPACE =
        createProperty("removeTrailingWhitespace", false);
    public static final Property RESTRICT_CARET =
        createProperty("restrictCaret", false);
    public static final Property SAVE_IN_PLACE =
        createProperty("saveInPlace", false);
    public static final Property SHOW_CHANGE_MARKS =
        createProperty("showChangeMarks", true);
    public static final Property SHOW_LINE_NUMBERS =
        createProperty("showLineNumbers", false);
    public static final Property SHOW_MESSAGE_NUMBERS =
        createProperty("showMessageNumbers", false);
    public static final Property SORT_BUFFER_LIST =
        createProperty("sortBufferList", false);
    public static final Property SSH_ECHO =
        createProperty("sshEcho", false);
    public static final Property STATUS_BAR_DISPLAY_LINE_COUNT =
        createProperty("StatusBar.displayLineCount", false);
    public static final Property STATUS_BAR_DISPLAY_LINE_SEPARATOR =
        createProperty("StatusBar.displayLineSeparator", false);
    public static final Property TAB_ALWAYS_INDENT =
        createProperty("tabAlwaysIndent", true);
    public static final Property TOOL_BAR_IS_ROLLOVER =
        createProperty("ToolBar.isRollover", true);
    public static final Property TOOL_BAR_SHOW_ICONS =
        createProperty("ToolBar.showIcons", true);
    public static final Property TOOL_BAR_SHOW_TEXT =
        createProperty("ToolBar.showText", true);
    public static final Property UNDELETE_ADVANCE_DOT =
        createProperty("undeleteAdvanceDot", true);
    public static final Property UNDERLINE_BOLD =
        createProperty("underlineBold", false);
    public static final Property UPPER_CASE_ATTRIBUTE_NAMES =
        createProperty("upperCaseAttributeNames", false);
    public static final Property UPPER_CASE_TAG_NAMES =
        createProperty("upperCaseTagNames", true);
    public static final Property USE_INCREMENTAL_FIND =
        createProperty("useIncrementalFind", false);
    public static final Property USE_MENU_MNEMONICS =
        createProperty("useMenuMnemonics", true);
    public static final Property USE_TABS =
        createProperty("useTabs", false);
    public static final Property WRAP =
        createProperty("wrap", false);

    // String properties with default values.
    public static final Property DEFAULT_ENCODING =
        createProperty("defaultEncoding", "ISO-8859-1");
    public static final Property DIALOG_FONT_NAME =
        createProperty("dialogFontName", "Dialog");
    public static final Property FONT_NAME =
        createProperty("fontName", "Monospaced");
    public static final Property PRINTER_FONT_NAME =
        createProperty("printerFontName", "Courier");
    public static final Property SHELL_PROMPT_PATTERN =
        createProperty("shellPromptPattern", DEFAULT_SHELL_PROMPT_PATTERN);
    public static final Property SSH_PROMPT_PATTERN =
        createProperty("sshPromptPattern", DEFAULT_SHELL_PROMPT_PATTERN);
    public static final Property TELNET_PROMPT_PATTERN =
        createProperty("telnetPromptPattern", DEFAULT_SHELL_PROMPT_PATTERN);
    public static final Property TEXT_FIELD_FONT_NAME =
        createProperty("textFieldFontName", "Monospaced");

    // String properties with no default values.
    public static final Property BACKUP_DIRECTORY =
        createProperty("backupDirectory");
    public static final Property BROWSER =
        createProperty("browser");
    public static final Property BROWSER_OPTS =
        createProperty("browserOpts");
    public static final Property DOC_PATH =
        createProperty("docPath");
    public static final Property EOM =
        createProperty("eom");
    public static final Property EXTENSION =
        createProperty("extension");
    public static final Property FCC =
        createProperty("fcc");
    public static final Property FILES =
        createProperty("files");
    public static final Property FTP_ANONYMOUS_PASSWORD =
        createProperty("ftpAnonymousPassword");
    public static final Property GUTTER_FONT_NAME =
        createProperty("gutterFontName");
    public static final Property HTML_MODE_TAGS =
        createProperty("HtmlMode.tags");
    public static final Property HTTP_USER_AGENT =
        createProperty("httpUserAgent");
    public static final Property INBOX =
        createProperty("inbox");
    public static final Property INCLUDE_PATH =
        createProperty("includePath");
    public static final Property JAVA_MODE_INSERT_COMMENT_TEXT =
        createProperty("JavaMode.insertCommentText");
    public static final Property JDB =
        createProperty("jdb");
    public static final Property JDK_DOC_PATH =
        createProperty("jdkDocPath");
    public static final Property JDK_SOURCE_PATH =
        createProperty("jdkSourcePath");
    public static final Property GLOBAL_KEY_MAP =
        createProperty("globalKeyMap");
    public static final Property LOOK_AND_FEEL =
        createProperty("lookAndFeel");
    public static final Property NEWS =
        createProperty("news");
    public static final Property ORG_XML_SAX_PARSER =
        createProperty("org.xml.sax.parser");
    public static final Property SHELL_FILE_NAME =
        createProperty("shellFileName");
    public static final Property SIGNATURE =
        createProperty("signature");
    public static final Property SMTP =
        createProperty("smtp");
    public static final Property SOURCE_PATH =
        createProperty("sourcePath");
    public static final Property SSH =
        createProperty("ssh");
    public static final Property STAMP_FORMAT =
        createProperty("stampFormat");
    public static final Property TAG_PATH =
        createProperty("tagPath");
    public static final Property TELNET =
        createProperty("telnet");
    public static final Property THEME =
        createProperty("theme");
    public static final Property THEME_PATH =
        createProperty("themePath");
    public static final Property TUNNEL =
        createProperty("tunnel");
    public static final Property USER_FULL_NAME =
        createProperty("userFullName");
    public static final Property USER_MAIL_ADDRESS =
        createProperty("userMailAddress");
    public static final Property WINDOW_MANAGER =
        createProperty("windowManager");

    // Color properties (no default value).
    public static final Property COLOR_BACKGROUND =
        createProperty("color.background");
    public static final Property COLOR_CARET = createProperty("color.caret");
    public static final Property COLOR_CHANGE = createProperty("color.change");
    public static final Property COLOR_SAVED_CHANGE =
        createProperty("color.savedChange");
    public static final Property COLOR_CURRENT_LINE_BACKGROUND =
        createProperty("color.currentLineBackground");
    public static final Property COLOR_GUTTER_BORDER =
        createProperty("color.gutterBorder");
    public static final Property COLOR_LINE_NUMBER =
        createProperty("color.lineNumber");
    public static final Property COLOR_MATCHING_BRACKET_BACKGROUND =
        createProperty("color.matchingBracketBackground");
    public static final Property COLOR_TEXT = createProperty("color.text");
    public static final Property COLOR_SELECTION_BACKGROUND =
        createProperty("color.selectionBackground");
    public static final Property COLOR_VERTICAL_RULE =
        createProperty("color.verticalRule");

    private final String displayName;
    private final String key;
    private Class type;
    private Object defaultValue;

    private Property(String key)
    {
        displayName = key;
        this.key = key.toLowerCase().intern();
    }

    private Property(String key, Object defaultValue)
    {
        displayName = key;
        this.key = key.toLowerCase().intern();
        this.defaultValue = defaultValue;
    }

    private Property(String key, boolean defaultValue)
    {
        displayName = key;
        this.key = key.toLowerCase().intern();
        this.type = Boolean.TYPE;
        this.defaultValue = defaultValue ? Boolean.TRUE : Boolean.FALSE;
    }

    private Property(String key, int defaultValue)
    {
        displayName = key;
        this.key = key.toLowerCase().intern();
        this.type = Integer.TYPE;
        this.defaultValue = new Integer(defaultValue);
    }

    private static Property createProperty(String key)
    {
        Property property = new Property(key);
        put(key, property);
        return property;
    }

    private static Property createProperty(String key, Object defaultValue)
    {
        Property property = new Property(key, defaultValue);
        put(key, property);
        return property;
    }

    private static Property createProperty(String key, boolean defaultValue)
    {
        Property property = new Property(key, defaultValue);
        put(key, property);
        return property;
    }

    private static Property createProperty(String key, int defaultValue)
    {
        Property property = new Property(key, defaultValue);
        put(key, property);
        return property;
    }

    private static void put(String key, Property property)
    {
        ht.put(key.toLowerCase(), property);
    }

    public static Property findProperty(String key)
    {
        return (Property) ht.get(key.toLowerCase());
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String key()
    {
        return key;
    }

    public Object getDefaultValue()
    {
        return defaultValue;
    }

    public boolean isBooleanProperty()
    {
        return type == Boolean.TYPE;
    }

    public boolean isIntegerProperty()
    {
        return type == Integer.TYPE;
    }

    public boolean validate(String value)
    {
        if (type == Boolean.TYPE) {
            if (value.equals("true") || value.equals("1"))
                return true;
            if (value.equals("false") || value.equals("0"))
                return true;
            return false;
        }
        if (type == Integer.TYPE) {
            try {
                Integer.parseInt(value);
                return true;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object obj)
    {
        return this == obj;
    }

    public int hashCode()
    {
        return key.hashCode();
    }

    public int compareTo(Object o)
    {
        Property p = (Property) o;
        return displayName.compareToIgnoreCase(p.displayName);
    }
}
