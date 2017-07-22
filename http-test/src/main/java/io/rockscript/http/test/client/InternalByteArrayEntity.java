/*
 * Copyright ©2017, RockScript.io. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.rockscript.http.test.client;

import java.io.*;

import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;

import static org.apache.http.util.Args.notNull;

class InternalByteArrayEntity extends AbstractHttpEntity implements Cloneable {

    private final byte[] b;
    private final int off, len;

    public InternalByteArrayEntity(final byte[] b, final ContentType contentType) {
        super();
        notNull(b, "Source byte array");
        this.b = b;
        this.off = 0;
        this.len = this.b.length;
        if (contentType!=null) {
            setContentType(contentType.toString());
        }
    }

    public InternalByteArrayEntity(final byte[] b, final int off, final int len, final ContentType contentType) {
        super();
        notNull(b, "Source byte array");
        if ((off<0) || (off>b.length) || (len<0) || ((off+len)<0) || ((off+len)>b.length)) {
            throw new IndexOutOfBoundsException("off: "+off+" len: "+len+" b.length: "+b.length);
        }
        this.b = b;
        this.off = off;
        this.len = len;
        if (contentType!=null) {
            setContentType(contentType.toString());
        }
    }

    public InternalByteArrayEntity(final byte[] b) {
        this(b, null);
    }

    public InternalByteArrayEntity(final byte[] b, final int off, final int len) {
        this(b, off, len, null);
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public long getContentLength() {
        return this.len;
    }

    @Override
    public InputStream getContent() {
        return new ByteArrayInputStream(this.b, this.off, this.len);
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        notNull(outstream, "Output stream");
        outstream.write(this.b, this.off, this.len);
        outstream.flush();
    }

    @Override
    public boolean isStreaming() {
        return false;
    }

}
