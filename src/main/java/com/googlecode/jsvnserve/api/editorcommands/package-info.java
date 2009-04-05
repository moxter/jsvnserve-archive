/*
 * Copyright 2009 The jSVNServe Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

/**
 *
 * <table>
 * <tr><td><code style="color:green">( target-rev (</code></td></tr>
 * <tr><td  rowspan="1"></td><td><code style="color:green">rev:number</code></td>
 *     <td>target revision number</td></tr>
 * <tr><td><code style="color:green">) )</code></td></tr>
 * <tr><td colspan="3">Defines the target revision this delta is running for.
 * E.g. the target revision to which an update is running.<br/>
 * See also: {@link EditorCommandSet#targetRevision}</td></tr>
 * <tr><td></td></tr>
 *
 * <tr><td><code style="color:green">( open-root (</code></td></tr>
 * <tr><td  rowspan="2"></td><td><code style="color:green">( ?rev:number )</code></td>
 *     <td>revision number of the root (optional, could be also defined via SVN
 *         properties)</td></tr>
 * <tr><td><code style="color:green">root-token:string</code></td>
 *     <td>token of the root path</td></tr>
 * <tr><td><code style="color:green">) )</code></td></tr>
 * <tr><td colspan="3">
 * Opens the root directory on which the operation was invoked.
 * All property changes as well as entries adding/deletion will be applied to
 * this root directory.
 * When coming back up to this root (after traversing the entire tree) you should close the root by calling closeDir().
 * </td></tr>
 * </table>

Deletes an entry.
In a commit - deletes an entry from a repository. In an update - deletes an entry locally (since it has been deleted in the repository). In a status - informs that an entry has been deleted.
( delete-entry ( path:string rev:number dir-token:string ))

Adds a directory.
In a commit - adds a new directory to a repository.
In an update - locally adds a directory that was added in the repository.
In a status - informs about a new directory scheduled for addition.
If copyFromPath is not null then it says that path is copied from copyFromPath located in copyFromRevision.
( add-dir ( path:string parent-token:string child-token:string ( ?copy-path:string ?copy-rev:number ) )

  open-dir
    params:   ( path:string parent-token:string child-token:string rev:number )

  change-dir-prop
    params:   ( dir-token:string name:string [ value:string ] )

  close-dir
    params:   ( dir-token:string )

  absent-dir
    params:   ( path:string parent-token:string )

  add-file
    params:   ( path:string dir-token:string file-token:string
                [ copy-path:string copy-rev:number ] )

  open-file
    params:   ( path:string dir-token:string file-token:string rev:number )

  apply-textdelta
    params:   ( file-token:string [ base-checksum:string ] )

  textdelta-chunk
    params: ( file-token:string chunk:string )

  textdelta-end
    params: ( file-token:string )

  change-file-prop
    params:   ( file-token:string name:string [ value:string ] )

  close-file
    params:   ( file-token:string [ text-checksum:string ] )

  absent-file
    params:   ( path:string parent-token:string )

  close-edit
    params:   ( )
    response: ( )

  abort-edit
    params:   ( )
    response: ( )

  finish-replay
    params:   ( )
    Only delivered from server to client, at the end of a replay.

 * @author jSVNServe Team
 * @version $Id: Editor.java 2 2009-03-30 19:33:30Z tim.moxter $
 */
package com.googlecode.jsvnserve.api.editorcommands;
