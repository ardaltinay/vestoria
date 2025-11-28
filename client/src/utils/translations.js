export const getBuildingTypeTr = (type) => {
  if (!type) return ''
  switch (type) {
    case 'SHOP':
      return 'DÜKKAN'
    case 'FARM':
      return 'ÇİFTLİK'
    case 'MINE':
      return 'MADEN'
    case 'FACTORY':
      return 'FABRİKA'
    case 'GARDEN':
      return 'BAHÇE'
    default:
      return type
  }
}

export const getBuildingSubTypeTr = (subType) => {
  if (!subType) return ''
  switch (subType) {
    // Shops
    case 'MARKET':
      return 'MARKET'
    case 'GREENGROCER':
      return 'MANAV'
    case 'CLOTHING':
      return 'GİYİM'
    case 'JEWELER':
      return 'KUYUMCU'

    // Production
    case 'GARDEN':
      return 'BAHÇE'
    case 'FARM':
      return 'ÇİFTLİK'
    case 'FACTORY':
      return 'FABRİKA'
    case 'MINE':
      return 'MADEN'

    default:
      return subType
  }
}

export const getItemUnitTr = (unit) => {
  if (!unit) return ''
  switch (unit) {
    case 'PIECE':
      return 'Adet'
    case 'KG':
      return 'Kg'
    case 'LITER':
      return 'Litre'
    case 'METER':
      return 'Metre'
    default:
      return unit
  }
}
