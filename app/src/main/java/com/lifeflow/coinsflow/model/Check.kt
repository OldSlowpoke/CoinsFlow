package com.lifeflow.coinsflow.model

import com.google.firestore.v1.StructuredAggregationQuery.Aggregation.Count

data class Check (
    var name: String = "",
    val amount: Double = 0.0,
    val count: Int = 0,
    val discount: Boolean = false
)
