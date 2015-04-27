package com.jesussoto.android.sparkleservice.sync;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import retrofit.mime.TypedOutput;

public class TypedOutputString implements TypedOutput {
    String string;

    public TypedOutputString(String string) {
        this.string = string;
    }

    @Override
    public String fileName() {
        return null;
    }

    @Override
    public String mimeType() {
        return null;
    }

    @Override
    public long length() {
        return 0;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(string.getBytes(Charset.forName("UTF-8")));
    }
}
