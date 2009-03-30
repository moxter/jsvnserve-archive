package com.googlecode.jsvnserve.api.delta;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.googlecode.jsvnserve.SVNServerSession;
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
    public void writeOpen(final SVNServerSession _session,
                          final String _parentToken)
            throws UnsupportedEncodingException, IOException
    {
        _session.writeItemList(
                new ListElement(Word.OPEN_ROOT,
                                new ListElement(new ListElement(),
                                                this.getToken())));
        this.writeAllProperties(_session, Word.CHANGE_DIR_PROP);
    }
}
