package com.googlecode.jsvnserve.api.delta;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.googlecode.jsvnserve.SVNSessionStreams;
import com.googlecode.jsvnserve.element.ListElement;
import com.googlecode.jsvnserve.element.WordElement.Word;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class DeltaRootOpen
        extends AbstractDeltaDirectory
{
    DeltaRootOpen(final Editor _deltaEditor,
                  final String _lastAuthor,
                  final Long _committedRevision,
                  final Date _committedDate)
    {
        super(_deltaEditor, 'd', "", _lastAuthor, _committedRevision, _committedDate);
    }

    @Override
    protected void writeOpen(final SVNSessionStreams _streams,
                             final String _parentToken)
            throws UnsupportedEncodingException, IOException
    {
        _streams.writeItemList(
                new ListElement(Word.OPEN_ROOT,
                                new ListElement(new ListElement(),
                                                this.getToken())));
        this.writeAllProperties(_streams, Word.CHANGE_DIR_PROP);
    }
}
