package com.kobal.FileStorageApp.filesecurity;

import java.nio.file.Path;

public interface SecurityService {

    boolean authorize(String principal, Path filepath);

}
