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

package com.googlecode.jsvnserve.api;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.jsvnserve.element.WordElement.Word;


/**
 * This class contains enumeration that describes depth,
 * that is used.
 * The order of these depths is important: the higher the number,
 * the deeper it descends.  You can use it to compare two depths
 * numerically to decide which goes deeper.
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public enum Depth
{
    /**
     * Depth undetermined or ignored.
     */
    UNKNOWN(Word.UNKNOWN),

    /**
     * Exclude (don't descend into) directory D.
     */
    EXCLUDE(Word.DEPTH_EXCLUDE),

    /**
     * Just the named directory D, no entries. For instance, update will not pull in
     * any files or sub directories.
     */
    EMPTY(Word.DEPTH_EMPTY),

    /**
     * D and its file children, but not sub directories. For instance, updates will pull in any
     * files, but not sub directories.
     */
    FILES(Word.DEPTH_FILES),

    /**
     * D and its immediate children (D and its entries).  Updates will pull in
     * any files or sub directories without any children.
     */
    IMMEDIATES(Word.DEPTH_IMMEDIATES),

    /**
     * D and all descendants (full recursion from D).  For instance, updates will pull
     * in any files or sub directories recursively.
     */
    INFINITY(Word.DEPTH_INFINITY);

    /**
     * Wrapper class used for static variables within enumerations to map
     * between {@link Word} elements and the related enumeration {@link Depth}.
     */
    private static final class Wrapper
    {
        /**
         * Maps between the related {@link Word}Êvalue and this depth
         * enumeration.
         *
         * @see Depth#valueOf(Word)
         */
        private static final Map<Word,Depth> MAP = new HashMap<Word,Depth>();
    }

    /**
     * Related word instance used within SVN communication.
     */
    public final Word word;

    /**
     * Simple constructor used to initialize {@link #word}.
     *
     * @param _word related {@link Word} element
     */
    private Depth(final Word _word)
    {
        this.word = _word;
        Depth.Wrapper.MAP.put(this.word, this);
    }

    /**
     * Depending on the {@link Word} element the related {@link Depth}
     * enumeration instance is returned.
     *
     * @param _word     word for which the {@link Depth} enumeration is search
     * @return found {@link Depth} enumeration instance; or <code>null</code>
     *         if not defined
     * @see Wrapper#MAP
     */
    public static Depth valueOf(final Word _word)
    {
        return Depth.Wrapper.MAP.get(_word);
    }
}
