declare namespace google.maps {
  type MapTypeStyle = {
    featureType?: string
    elementType?: string
    stylers: Record<string, string | number | boolean>[]
  }

  type LatLngLiteral = { lat: number; lng: number }

  class Map {
    constructor(element: HTMLElement, options: Record<string, unknown>)
    fitBounds(bounds: LatLngBounds, padding?: number | Record<string, number>): void
    panTo(latLng: LatLngLiteral): void
  }

  class Marker {
    constructor(options: Record<string, unknown>)
    addListener(eventName: string, handler: () => void): void
    setMap(map: Map | null): void
  }

  class InfoWindow {
    constructor(options?: Record<string, unknown>)
    setContent(content: string | Node): void
    open(options: { map: Map; anchor?: Marker }): void
    close(): void
  }

  class LatLngBounds {
    constructor()
    extend(point: LatLngLiteral): void
  }

  class Size {
    constructor(width: number, height: number)
  }

  class Point {
    constructor(x: number, y: number)
  }
}

interface Window {
  google: { maps: typeof google.maps }
  [key: `__eventVerseGoogleMapsReady_${string}`]: (() => void) | undefined
}