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

        val transactions = mutableMapOf<String, MutableMap<String, BigDecimal>>()

        val debtList = MutableList(participants.size) { BigDecimal(0) }
        val creditList = MutableList(participants.size) { BigDecimal(0) }
        val balanceList = MutableList(participants.size) { BigDecimal(0) }

        for (spending in spendings) {
            creditList[participants.indexOf(spending.payer)] += sum(spending.parts)
            for (i in spending.parts.indices) {
                debtList[i] += spending.parts[i]
            }
        }

        for (i in debtList.indices) {
            balanceList[i] = creditList[i] - debtList[i]
        }

        while (true) {
            val maxI = indexOfMax(balanceList)
            val minI = indexOfMin(balanceList)
            val currentMax = balanceList[maxI]
            val currentMin = balanceList[minI]

            if (currentMax == BigDecimal.ZERO) break

            val min = minOf(currentMax, -currentMin)

            writeTransaction(transactions, participants[minI], participants[maxI], min)

            balanceList[maxI] -= min
            balanceList[minI] += min

        }

        return OutputData(transactions.keys.toList(), transactions)
    }

    private fun sum(spendings: List<BigDecimal>): BigDecimal {
        var sum = BigDecimal(0)
        spendings.forEach { spending -> sum += spending }
        return sum
    }

    private fun indexOfMin(spendings: List<BigDecimal>): Int {
        return spendings.indexOf(spendings.minOrNull())
    }

    private fun indexOfMax(spendings: List<BigDecimal>): Int {
        return spendings.indexOf(spendings.maxOrNull())
    }

    private fun writeTransaction(
        transaction: MutableMap<String, MutableMap<String, BigDecimal>>,
        payer: String,
        payee: String,
        amount: BigDecimal
    ) {
        if (transaction.containsKey(payer)) {
            if (transaction[payer]!!.containsKey(payee)) transaction[payer]!![payee]!!.add(amount)
            else transaction[payer]!![payee] = amount
        } else transaction.put(
            payer, mutableMapOf(
                Pair(
                    payee, amount
                )
            )
        )
    }

}