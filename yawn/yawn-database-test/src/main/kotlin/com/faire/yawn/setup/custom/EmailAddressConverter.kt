package com.faire.yawn.setup.custom

import javax.persistence.AttributeConverter

internal class EmailAddressConverter : AttributeConverter<EmailAddress, String> {
  override fun convertToDatabaseColumn(attribute: EmailAddress?): String? {
    return attribute?.emailAddress
  }

  override fun convertToEntityAttribute(string: String?): EmailAddress? {
    return string?.let { EmailAddress(it) }
  }
}
