import { Injectable } from "@angular/core";
import {
  NotificationHorizontalPosition,
  NotificationService,
  NotificationTypes,
  NotificationVerticalPosition,
} from "./notification-service.service";
import { IProductDto } from "./rest-client-dtos/IProductDto";

@Injectable({
  providedIn: "root",
})
export class CartService {
  private readonly localStorageKey = "cart";
  private readonly setCurrentCart = (cart: Array<ICartItem>) =>
    localStorage.setItem(this.localStorageKey, JSON.stringify(cart));

  constructor(private readonly notificationService: NotificationService) {}

  readonly getCurrentCart = () =>
    (JSON.parse(localStorage.getItem(this.localStorageKey)) ||
      []) as Array<ICartItem>;

  addProduct(product: IProductDto): void {
    const currentCart = this.getCurrentCart();
    currentCart.push({
      name: product.name,
      pictureUrl: product.productImages[0].path, // xd
      price: product.price,
    } as ICartItem);
    this.setCurrentCart(currentCart);

    this.notificationService.showNotification(
      NotificationVerticalPosition.Top,
      NotificationHorizontalPosition.Center,
      `Product ${product.name} has been added to cart`,
      NotificationTypes.Success
    );
  }

  removeProduct(product: IProductDto): void {
    const productName = product.name;
    const currentCart = this.getCurrentCart();
    const newCart = currentCart.filter(
      (product) => product.name !== productName
    );
    this.setCurrentCart(newCart);

    this.notificationService.showNotification(
      NotificationVerticalPosition.Top,
      NotificationHorizontalPosition.Center,
      `Product has been removed from cart`,
      NotificationTypes.Success
    );
  }

  checkIfAlreadyExists(product: IProductDto): boolean {
    const productName = product.name;
    const currentCart = this.getCurrentCart();

    return !!currentCart.find((product) => product.name === productName);
  }

  clearCart(): void {
    localStorage.setItem(this.localStorageKey, null);
  }
}

export interface ICartItem {
  name: string;
  price: number;
  productId: number;
  pictureUrl: string;
}