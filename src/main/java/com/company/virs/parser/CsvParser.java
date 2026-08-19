package com.company.virs.parser;

import com.company.virs.dto.request.InventoryRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CsvParser {

    List<InventoryRequest> parse(
            MultipartFile file);
}
