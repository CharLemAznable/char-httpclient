package com.github.charlemaznable.httpclient.ohclient.internal;

import com.github.charlemaznable.httpclient.common.CommonBase;
import com.github.charlemaznable.httpclient.ohclient.elf.GlobalClientElf;
import lombok.NoArgsConstructor;
import okhttp3.OkHttpClient;

@NoArgsConstructor
final class OhBase extends CommonBase<OhBase> {

    static final OhBase DEFAULT = new OhBase();

    static {
        DEFAULT.client = GlobalClientElf.globalClient();
    }

    OkHttpClient client;

    public OhBase(OhBase other) {
        super(other);
        this.client = other.client;
    }
}
