package org.jaqpot.api.service.dataset.csv

import jakarta.ws.rs.BadRequestException

class CSVParserException(message: String, cause: Throwable) : BadRequestException(message, cause)
