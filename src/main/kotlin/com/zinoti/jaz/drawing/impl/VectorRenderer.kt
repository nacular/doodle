package com.zinoti.jaz.drawing.impl

import com.zinoti.jaz.drawing.Renderer


/**
 * VectorRenderers provide vector rendering implementations.
 */

interface VectorRenderer: Renderer

typealias VectorRendererFactory = (CanvasContext) -> VectorRenderer