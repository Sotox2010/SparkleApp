package com.jesussoto.android.sparkle.service;

import java.io.IOException;
import java.io.InputStream;

import retrofit.mime.TypedInput;

public class TypedInputString implements TypedInput {
    @Override
    public String mimeType() {
        return null;
    }

    @Override
    public long length() {
        return 0;
    }

    @Override
    public InputStream in() throws IOException {
        return null;
    }
}
