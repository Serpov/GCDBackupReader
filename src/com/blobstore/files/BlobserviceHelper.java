package com.blobstore.files;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;

public class BlobserviceHelper {

    public static RecordReadChannel openRecordReadChannel(BlobKey blobKey,
                                                          BlobstoreService blobstoreService) {
        final FileReadChannel channel = new FileReadChannelImpl(blobKey, blobstoreService);
        final FileReadChannel fileReadChannel = new BufferedFileReadChannelImpl(channel, RecordConstants.BLOCK_SIZE * 2);
        return new RecordReadChannelImpl(fileReadChannel);
    }

}
