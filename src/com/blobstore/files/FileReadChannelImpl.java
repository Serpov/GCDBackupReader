package com.blobstore.files;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.repackaged.com.google.protobuf.ByteString;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

/**
 * An implementation of {@code FileReadChannel}.
 */
class FileReadChannelImpl implements FileReadChannel {

    private final BlobKey blobKey;
    private BlobstoreService blobstoreService;

    private long position;
    private boolean isOpen;
    private boolean reachedEOF;
    private final Object lock = new Object();

    FileReadChannelImpl(BlobKey blobKey,
                        BlobstoreService bs) {
        this.blobKey = blobKey;
        this.blobstoreService = bs;
        isOpen = true;
        reachedEOF = false;
        if (null == bs) {
            throw new NullPointerException("fs is null");
        }
    }

    private void checkOpen() throws ClosedChannelException {
        synchronized (lock) {
            if (!isOpen) {
                throw new ClosedChannelException();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws ClosedChannelException
     */
    @Override
    public long position() throws ClosedChannelException {
        synchronized (lock) {
            checkOpen();
            return position;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileReadChannel position(long newPosition) throws IOException {
        if (newPosition < 0) {
            throw new IllegalArgumentException("newPosition may not be negative");
        }
        synchronized (lock) {
            checkOpen();
            position = newPosition;
            reachedEOF = false;
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(ByteBuffer dst) throws IOException {
        synchronized (lock) {
            if (reachedEOF) {
                return -1;
            }
            int numBytesRead = readInternal(dst);
            if (numBytesRead >= 0) {
                position += numBytesRead;
            } else {
                reachedEOF = true;
            }
            return numBytesRead;
        }
    }

    private int readInternal(ByteBuffer buffer) {
        if (position < 0) {
            throw new IllegalArgumentException("startingPos is negative: " + position);
        }
        if (buffer == null) {
            throw new NullPointerException("buffer is null");
        }
        long remaining = buffer.remaining();
        if (buffer.remaining() < 1) {
            return 0;
        }

        final ByteString byteString = ByteString.copyFrom(blobstoreService.fetchData(blobKey, position, position + remaining - 1));
        int numBytesRead = byteString.size();

        byteString.copyTo(buffer);

        if (numBytesRead <= 0) {
            numBytesRead = -1;
        }
        return numBytesRead;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpen() {
        synchronized (lock) {
            return isOpen;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (!isOpen) {
                return;
            }
            isOpen = false;
        }
    }
}
