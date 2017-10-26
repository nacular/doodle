package com.nectar.doodle.drawing.impl

import com.nectar.doodle.drawing.Renderer


/**
 * VectorRenderers provide vector rendering implementations.
 */

interface VectorRenderer: Renderer

typealias VectorRendererFactory = (CanvasContext) -> VectorRenderer