package victor.training.reactive.usecase.complex

data class Product(
    val id: Long, // long
    val name: String,
    val isActive: Boolean,
    val isResealed: Boolean,
    val rating: ProductRatingResponse? = null,
)

fun Product.withRating(productRatingResponse: ProductRatingResponse): Product =
    Product(this.id, this.name, this.isActive, this.isResealed, productRatingResponse)