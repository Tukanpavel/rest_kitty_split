package ru.sau.kitty_split.split.service

import org.springframework.stereotype.Service
import java.math.BigDecimal

interface SplitService {
    fun splitBill(inputData: InputData): OutputData
}

@Service
class SplitServiceStub : SplitService {
    override fun splitBill(inputData: InputData): OutputData {
        val participants = inputData.participants
        val spendings = inputData.spendings

        val debt: MutableMap<String, MutableList<BigDecimal>> = mutableMapOf()

        participants.forEach { participant ->
            debt[participant] = spendings.asSequence().filter { spending ->
                spending.payer == participant
            }.map { spending -> spending.parts }.toList()[0].toMutableList()
        }

        for (spending in spendings) {
            participants.forEach { participant ->
                for (i in 0..participants.size) {
                    debt[participant]!![participants.indexOf(participant)] -= spending.parts[i]
                }
            }
        }
        debt.filter { (participant, spending) -> !sum(spending).equals(0) }

        val transactions = simpleTransactions(debt)

        return OutputData(transactions.keys.toList(), transactions)
    }

    private fun sum(spendings: List<BigDecimal>): BigDecimal {
        var sum = BigDecimal(0)
        spendings.forEach { spending -> sum += spending }
        return sum
    }

    private fun simpleTransactions(debt: MutableMap<String, MutableList<BigDecimal>>): Map<String, Map<String, BigDecimal>> {
        val myMap: Map<String, Map<String, BigDecimal>> = mutableMapOf()

        val paymentList = debt.map{ (participant, values) ->  Pair(participant,sum(values)) }

        return myMap
    }
}