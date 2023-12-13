package com.tecdo.job.foreign.pixalate;

import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ftp.FtpMode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by Zeki on 2023/12/5
 */
@Component
@RequiredArgsConstructor
public class PixalateFtpBuilder {

    @Value("${foreign.pixalate.host}")
    private String host;
    @Value("${foreign.pixalate.port:21}")
    private int port;
    @Value("${foreign.pixalate.user}")
    private String user;
    @Value("${foreign.pixalate.password}")
    private String password;

    public Ftp build() {
        Ftp ftp = new Ftp(host, port, user, password);
        ftp.setMode(FtpMode.Passive);
        return ftp;
    }
}
