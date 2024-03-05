package io.milk.products

class ProductService(private val dataGateway: ProductDataGateway) {
    fun findAll(): List<ProductInfo> {
        return dataGateway.findAll().map { ProductInfo(it.id, it.name, it.quantity) }
    }

    fun findBy(id: Long): ProductInfo {
        val record = dataGateway.findBy(id)!!
        return ProductInfo(record.id, record.name, record.quantity)
    }

    fun update(purchase: PurchaseInfo): ProductInfo {
        val record = dataGateway.findBy(purchase.id)!!
        record.quantity -= purchase.amount
        dataGateway.update(record)
        return findBy(record.id)
    }

    fun decrementBy(purchase: PurchaseInfo) {
        // TODO - Implement the function.

        // use the Gateway class' logged decrement function
        // this decrements the value in the database atomically and within a transaction.
        dataGateway.fasterDecrementBy(purchase)
    }
}