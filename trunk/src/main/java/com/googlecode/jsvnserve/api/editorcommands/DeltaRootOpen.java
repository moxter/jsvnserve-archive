package com.googlecode.jsvnserve.api.editorcommands;

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
    DeltaRootOpen(final String _token,
                  final String _lastAuthor,
                  final Long _committedRevision,
                  final Date _committedDate)
    {
        super(_token, "", null, null, _lastAuthor, _committedRevision, _committedDate);
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
