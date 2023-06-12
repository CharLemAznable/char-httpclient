package com.github.charlemaznable.httpclient.wfclient.internal;

import com.github.charlemaznable.httpclient.common.CommonBase;
import com.github.charlemaznable.httpclient.wfclient.elf.GlobalClientElf;
import lombok.NoArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

@NoArgsConstructor
final class WfBase extends CommonBase<WfBase> {

    static final WfBase DEFAULT = new WfBase();

    static {
        DEFAULT.client = GlobalClientElf.globalClient();
    }

    WebClient client;

    public WfBase(WfBase other) {
        super(other);
        this.client = other.client;
    }
}
