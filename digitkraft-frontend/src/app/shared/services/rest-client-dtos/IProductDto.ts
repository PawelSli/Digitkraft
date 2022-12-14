import { ICategoryDto } from "./ICategoryDto";
import { ICopyDto } from "./ICopyDto";
import { IProductImageDto } from "./IProductImageDto";

export interface IProductDto {
  name: string;
  price: number;
  description: string;
  category: ICategoryDto;
  copies: ICopyDto;
  productImages: Array<IProductImageDto>;
}
