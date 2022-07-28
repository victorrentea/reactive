package victor.training.reactive.usecase.complex

class ProductDetailsResponse(
    var id: Long? = null,
    var name: String? = null,
    var isActive:Boolean = false,
    var isResealed:Boolean = false,
) {
    fun toEntity(): Product {
        return Product(id!!, name!!, isActive, isResealed, null)
    }

}