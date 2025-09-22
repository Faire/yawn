package com.faire.yawn.setup.entities

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
internal class BookSales {
  @Column
  var paperBacksSold: Long = 0

  @Column
  var hardBacksSold: Long = 0

  @Column
  var eBooksSold: Long = 0

  @Column
  var countryWithMostCopiesSold: String = ""

  // Default no-argument constructor required by Hibernate
  constructor()

  constructor(
      paperBacksSold: Long = 0,
      hardBacksSold: Long = 0,
      eBooksSold: Long = 0,
      countryWithMostCopiesSold: String = "",
  ) {
    this.paperBacksSold = paperBacksSold
    this.hardBacksSold = hardBacksSold
    this.eBooksSold = eBooksSold
    this.countryWithMostCopiesSold = countryWithMostCopiesSold
  }
}
