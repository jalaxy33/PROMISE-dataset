/*
 * OptionPane.java - Option pane interface
 * Copyright (C) 1999 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
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

package org.gjt.sp.jedit;

import java.awt.Component;

/**
 * The interface all option panes must implement.  Internally, jEdit uses
 * option panes to implement the tabs in the "Global Options" dialog box.
 * Plugins can also create option panes for the "Plugin Options" dialog
 * box.<p>
 *
 * The <i>name</i> of an option pane is returned by the <code>getName()</code>
 * method. The label displayed in the option pane's tab is obtained from the
 * <code>options.<i>name</i>.label</code> property.
 *
 * @see org.gjt.sp.jedit.AbstractOptionPane
 */
public interface OptionPane
{
	/**
	 * Returns the internal name of this option pane.
	 */
	String getName();

	/**
	 * Returns the component that should be displayed for this option pane.
	 */
	Component getComponent();

	/**
	 * This method should create the option pane's GUI.
	 */
	void init();

	/**
	 * Called when the options dialog's "ok" button is clicked.
	 * This should save any properties being edited in this option
	 * pane.
	 */
	void save();
}

/*
 * ChangeLog:
 * $Log: OptionPane.java,v $
 * Revision 1.8  2000/04/28 09:29:11  sp
 * Key binding handling improved, VFS updates, some other stuff
 *
 * Revision 1.7  2000/04/23 03:58:00  sp
 * ContextOptionPane didn't compile, hack to let JBrowse and QuickFile work
 *
 * Revision 1.6  2000/04/16 08:56:24  sp
 * Option pane updates
 *
 * Revision 1.5  1999/11/21 01:20:30  sp
 * Bug fixes, EditBus updates, fixed some warnings generated by jikes +P
 *
 * Revision 1.4  1999/10/04 03:20:51  sp
 * Option pane change, minor tweaks and bug fixes
 *
 * Revision 1.3  1999/10/02 01:12:36  sp
 * Search and replace updates (doesn't work yet), some actions moved to TextTools
 *
 * Revision 1.2  1999/05/01 00:55:11  sp
 * Option pane updates (new, easier API), syntax colorizing updates
 *
 * Revision 1.1  1999/04/21 07:39:18  sp
 * FAQ added, plugins can now add panels to the options dialog
 *
 */
