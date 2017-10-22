//package com.zinoti.jaz.drawing.impl;
//
//import java.util.*;
//
//import com.google.common.base.*;
//import com.google.common.collect.Maps;
//
//import com.zinoti.jaz.core.Container;
//import com.zinoti.jaz.core.Display;
//import com.zinoti.jaz.ui.*;
//import com.zinoti.jaz.core.*;
//import com.zinoti.jaz.util.*;
//import com.zinoti.jaz.task.*;
//import com.zinoti.jaz.event.*;
//import com.zinoti.jaz.drawing.*;
//import com.zinoti.jaz.geometry.*;
//import com.zinoti.jaz.event.BoundsEvent.*;
//
//
//public final class RenderManagerImpl implements RenderManager
//{
//    public RenderManagerImpl(Display aDisplay, UIManager aUIManager, TaskFactory aTaskManager )
//    {
//        Preconditions.checkArgument( aDisplay     != null, "Display cannot be null"   );
//        Preconditions.checkArgument( aUIManager   != null, "UIManager cannot be null" );
//        Preconditions.checkArgument( aTaskManager != null, "TaskManager cannot be null" );
//
//        mDisplay             = aDisplay;
//        mUIManager           = aUIManager;
//
//        mDisplayTree         = Maps.newHashMap();
//
//        mGizmos              = new HashSet<Gizmo>    ();
//        mLayingOut           = null;
//        mDirtyGizmos         = new HashSet<Gizmo>    ();
//        mNeverRendered       = new HashSet<Gizmo>    ();
//        mPendingLayout       = new HashSet<Container>();
//        mPendingRender       = new ArrayList<Gizmo>  ();
//        mPendingCleanup      = new HashMap<Container, Set<Gizmo>>();
//        mVisibilityChanged   = new HashSet<Gizmo>    ();
//        mPendingBoundsChange = new HashSet<Gizmo>    ();
//
//        mAddedInvisible      = new HashSet<Gizmo>    ();
//
//        mTask                = aTaskManager.createTask( new RenderCommand() );
//        mBoundsListener      = new InternalBoundsListener   ();
//        mPropertyListener    = new InternalPropertyListener ();
//        mContainerListener   = new InternalContainerListener();
//
//        mDisplay.addContainerListener( mContainerListener );
//        mDisplay.addBoundsListener   ( new BoundsListener()
//        {
//            @Override public void boundsChanged( BoundsEvent aBoundsEvent )
//            {
//                Container aContainer = (Container)aBoundsEvent.getSource();
//
//                aContainer.doLayout();
//
//                if( mDisplayTree.containsKey( aContainer ) ) { checkDisplayRectChange( aContainer ); }
//            }
//        });
//
//        for( Gizmo aChild : mDisplay )
//        {
//            recordGizmo( aChild );
//        }
//    }
//
//    @Override public void render( Gizmo gizmo)
//    {
//        Preconditions.checkArgument(gizmo != null, "Gizmo cannot be null" );
//
//        render(gizmo, false );
//    }
//
//    @Override public void renderNow( Gizmo gizmo)
//    {
//        Preconditions.checkArgument(gizmo != null, "Gizmo cannot be null" );
//
//        if( mGizmos.contains(gizmo)    &&
//            !gizmo.getBounds().isEmpty() &&
//            mDisplay.isAncestor(gizmo) )
//        {
//            mDirtyGizmos.add(gizmo);
//
//            if( mPendingLayout.contains(gizmo) )
//            {
//                performLayout( (Container) gizmo);
//            }
//
//            Container aParent = gizmo.getParent();
//
//            if( mNeverRendered.contains( aParent ) ||
//                mDirtyGizmos.contains  ( aParent ) )
//            {
//                renderNow( aParent );
//            }
//            else
//            {
//                performRender(gizmo);
//            }
//        }
//    }
//
//    @Override public Rectangle getDisplayRect( Gizmo gizmo)
//    {
//        Preconditions.checkArgument(gizmo != null, "Gizmo cannot be null" );
//
//        return getDisplayRect(gizmo, gizmo.getSize() );
//    }
//
//    private Rectangle getDisplayRect( Gizmo aGizmo, Dimension aSize )
//    {
//        Container aParent = aGizmo.getParent();
//
//        if( aParent == null ) { return Rectangle.create(); }
//
//        DisplayRectNode aNode = mDisplayTree.get( aGizmo );
//
//        if( aNode != null ) { return aNode.getClipRect(); }
//
//        Rectangle aClipRect = aGizmo.isVisible(                                                 ) ?
//                              Rectangle.create( 0, 0, aSize.getWidth(), aSize.getHeight() ) :
//                              Rectangle.create(                                                 );
//
//        while( !aClipRect.isEmpty() && aParent != null )
//        {
//            aClipRect = aClipRect.intersect( Rectangle.create( 0 - aGizmo.getX  (),
//                                                               0 - aGizmo.getY  (),
//                                                               aParent.isVisible() ? aParent.getWidth () : 0,
//                                                               aParent.isVisible() ? aParent.getHeight() : 0 ) );
//
//            aGizmo  = aParent;
//            aParent = aParent.getParent();
//        }
//
//        return aClipRect;
//    }
//
//    private void schedulePaint()
//    {
//        if( !mTask.isScheduled() )
//        {
//            mTask.schedule( sPaintDelay );
//        }
//    }
//
//    private void render( Gizmo aGizmo, boolean aIgnoreEmptyBounds )
//    {
//        if( prepareRender( aGizmo, aIgnoreEmptyBounds ) ) { schedulePaint(); }
//    }
//
//    private boolean prepareRender( Gizmo aGizmo, boolean aIgnoreEmptyBounds )
//    {
//        if( ( aIgnoreEmptyBounds || !aGizmo.getBounds().isEmpty() ) &&
//            mGizmos.contains( aGizmo )                              &&
//            mDisplay.isAncestor( aGizmo ) )
//        {
//            mDirtyGizmos.add( aGizmo );
//
//            Iterator<Gizmo> aIterator = mPendingRender.iterator();
//
//            while( aIterator.hasNext() )
//            {
//                Gizmo aItem = aIterator.next();
//
//                // Only take reference identity into account
//                if( aItem == aGizmo || isAncestor( aItem, aGizmo ) ) { return false; }
//
//                if( isAncestor( aGizmo, aItem ) ) { aIterator.remove(); }
//            }
//
//            mPendingRender.add( aGizmo );
//
//            return true;
//        }
//
//        return false;
//    }
//
//    private boolean isAncestor( Gizmo aFirst, Gizmo aSecond )
//    {
//        return aFirst instanceof Container && ((Container)aFirst).isAncestor( aSecond );
//    }
//
//    private void onPaint()
//    {
//        do
//        {
//            Set<Container> aCopy = mPendingLayout;
//
//            mPendingLayout = new HashSet<Container>();
//
//            for( Container aContainer : aCopy )
//            {
//                performLayout( aContainer );
//            }
//
//            mLayingOut = null;
//        }
//        while( !mPendingLayout.isEmpty() );
//
//        List<Gizmo> aCopy = new ArrayList<Gizmo>( mPendingRender );
//
//        for( Gizmo aGizmo : aCopy )
//        {
//            performRender( aGizmo );
//        }
//
//        for( Gizmo aGizmo : mPendingBoundsChange )
//        {
//            if( !mNeverRendered.contains( aGizmo ) )
//            {
//                GraphicsSurface aGraphicsSurface = GraphicsService.locator().getGraphicsDevice().get( aGizmo );
//
//                if( aGraphicsSurface != null )
//                {
//                    updateGraphicsSurface( aGizmo, aGraphicsSurface );
//                }
//            }
//        }
//    }
//
//    private void performLayout( Container aContainer )
//    {
//        mLayingOut = aContainer;
//
//        aContainer.doLayout();
//    }
//
//    private void performRender( Gizmo aGizmo )
//    {
//        mPendingRender.remove( aGizmo );
//        mNeverRendered.remove( aGizmo );
//
//        boolean aVisibilityChanged = mVisibilityChanged.contains( aGizmo );
//
//        GraphicsSurface aGraphicsSurface = aGizmo.isVisible() || aVisibilityChanged ? GraphicsService.locator().getGraphicsDevice().get( aGizmo ) : null;
//
//        if( aGraphicsSurface != null )
//        {
//            if( mPendingBoundsChange.contains( aGizmo ) )
//            {
//                updateGraphicsSurface( aGizmo, aGraphicsSurface );
//
//                mPendingBoundsChange.remove( aGizmo );
//            }
//
//            if( aVisibilityChanged )
//            {
//                aGraphicsSurface.setVisible( aGizmo.isVisible() );
//
//                mVisibilityChanged.remove( aGizmo );
//            }
//
//            if( aGizmo.isVisible() && !aGizmo.getBounds().isEmpty() )
//            {
//                boolean aWasRendered = false;
//
//                if( aGizmo instanceof Container )
//                {
//                    Set<Gizmo> aGizmoList = mPendingCleanup.get( aGizmo );
//
//                    if( aGizmoList != null )
//                    {
//                        for( Gizmo aItem : aGizmoList )
//                        {
//                            releaseResources( aItem );
//
//                            GraphicsService.locator().getGraphicsDevice().release( aItem );
//                        }
//
//                        mPendingCleanup.remove( aGizmo );
//                    }
//                }
//
//                if( mDirtyGizmos.contains( aGizmo ) )
//                {
//                    mDirtyGizmos.remove( aGizmo );
//
//                    aGraphicsSurface.beginRender();
//
//                    aGizmo.render( aGraphicsSurface.getCanvas() );
//
//                    aWasRendered = true;
//                }
//
//                if( aGizmo instanceof Container )
//                {
//                    List<Gizmo> aChildren = ((Container)aGizmo).getChildrenByZIndex();
//
//                    for( int i = aChildren.size() - 1; i >= 0; --i )
//                    {
//                        performRender( aChildren.get( i ) );
//                    }
//                }
//
//                if( aWasRendered ) { aGraphicsSurface.endRender(); }
//            }
//        }
//    }
//
//    private void recordGizmo( Gizmo aGizmo )
//    {
//        if( aGizmo != null && !mGizmos.contains( aGizmo ) )
//        {
//            aGizmo.addedToDisplay( this, mUIManager );
//
//            mGizmos.add             ( aGizmo );
//            mDirtyGizmos.add        ( aGizmo );
//            mNeverRendered.add      ( aGizmo );
//            mPendingBoundsChange.add( aGizmo );
//
//            aGizmo.addBoundsListener  ( mBoundsListener   );
//            aGizmo.addPropertyListener( mPropertyListener );
//
//            if( aGizmo instanceof Container )
//            {
//                Container aContainer = (Container)aGizmo;
//
//                aContainer.addContainerListener( mContainerListener );
//
//                for( Gizmo aChild : aContainer )
//                {
//                    recordGizmo( aChild );
//                }
//
//                scheduleLayout( aContainer );
//            }
//
//            if( aGizmo.getDisplayRectHandlingEnabled() )
//            {
//                registerDisplayRectMonitoring( aGizmo );
//
//                notifyDisplayRectChange( aGizmo,
//                                         Rectangle.EMPTY,
//                                         getDisplayRect( aGizmo ) );
//            }
//
//            if( mDisplayTree.containsKey( aGizmo ) )
//            {
//                // TODO: IMPLEMENT
//            }
//
//            if( mDisplay.contains( aGizmo ) ) { render( aGizmo, true ); }
//        }
//    }
//
//    private void updateGraphicsSurface( Gizmo aGizmo, GraphicsSurface aGraphicsSurface )
//    {
//        aGraphicsSurface.setX     ( aGizmo.getX     () );
//        aGraphicsSurface.setY     ( aGizmo.getY     () );
//        aGraphicsSurface.setWidth ( aGizmo.getWidth () );
//        aGraphicsSurface.setHeight( aGizmo.getHeight() );
//    }
//
//    private void releaseResources( Gizmo aGizmo )
//    {
//        aGizmo.removedFromDisplay();
//
//        if( aGizmo instanceof Container )
//        {
//            for( Gizmo aChild : (Container)aGizmo )
//            {
//                releaseResources( aChild );
//            }
//
//            ((Container)aGizmo).removeContainerListener( mContainerListener );
//        }
//
//        mGizmos.remove             ( aGizmo );
//        mDirtyGizmos.remove        ( aGizmo );
//        mPendingLayout.remove      ( aGizmo );
//        mPendingRender.remove      ( aGizmo );
//        mPendingBoundsChange.remove( aGizmo );
//
//        aGizmo.removeBoundsListener  ( mBoundsListener   );
//        aGizmo.removePropertyListener( mPropertyListener );
//
//        unregisterDisplayRectMonitoring( aGizmo );
//    }
//
//    private void addToCleanupList( Container aContainer, Gizmo aChild )
//    {
//        Set<Gizmo> aGizmos = mPendingCleanup.get( aContainer );
//
//        if( aGizmos == null ) { aGizmos = new HashSet<Gizmo>(); }
//
//        aGizmos.add( aChild );
//
////        releaseResources( aChild );
//
//        mPendingCleanup.put( aContainer, aGizmos );
//    }
//
//    private void removeFromCleanupList( Container aContainer, Gizmo aChild )
//    {
//        Set<Gizmo> aGizmos = mPendingCleanup.get( aContainer );
//
//        if( aGizmos != null )
//        {
//            aGizmos.remove( aChild );
//
//            if( aGizmos.isEmpty() ) { mPendingCleanup.remove( aContainer ); }
//        }
//    }
//
//    private void registerDisplayRectMonitoring( Gizmo aGizmo )
//    {
//        if( aGizmo != null && !mDisplayTree.containsKey( aGizmo ) )
//        {
//            DisplayRectNode aNode = new DisplayRectNode( aGizmo );
//
//            aNode.setClipRect( Rectangle.create( 0, 0, aGizmo.getWidth(), aGizmo.getHeight() ) );
//
//            mDisplayTree.put( aGizmo, aNode );
//
//            Container aParent = aGizmo.getParent();
//
//            if( aParent != null )
//            {
//                registerDisplayRectMonitoring( aParent );
//
//                DisplayRectNode aParentNode = mDisplayTree.get( aParent );
//
//                updateClipRect( aNode, aParentNode );
//
//                aParentNode.add( aNode );
//            }
//        }
//    }
//
//    private void unregisterDisplayRectMonitoring( Gizmo aGizmo )
//    {
//        if( aGizmo != null )
//        {
//            DisplayRectNode aNode = mDisplayTree.get( aGizmo );
//
//            if( aNode != null && aNode.getNumChildren() == 0 )
//            {
//                mDisplayTree.remove( aGizmo );
//
//                DisplayRectNode aParent = aNode.getParent();
//
//                if( aParent != null )
//                {
//                    aParent.remove( aNode );
//                }
//
//                unregisterDisplayRectMonitoring( aGizmo.getParent() );
//            }
//        }
//    }
//
//    private void checkDisplayRectChange( Gizmo aGizmo )
//    {
//        DisplayRectNode aNode = mDisplayTree.get( aGizmo );
//
//        Rectangle aOldDisplayRect = aNode.getClipRect();
//
//        updateClipRect( aNode, mDisplayTree.get( aGizmo.getParent() ) );
//
//        if( !aOldDisplayRect.equals( aNode.getClipRect() ) )
//        {
//            if( aGizmo.getDisplayRectHandlingEnabled() )
//            {
//                notifyDisplayRectChange( aGizmo, aOldDisplayRect, aNode.getClipRect() );
//            }
//
//            for( int i=0; i < aNode.getNumChildren(); ++i )
//            {
//                checkDisplayRectChange( aNode.get( i ).getGizmo() );
//            }
//        }
//    }
//
//    private void notifyDisplayRectChange( Gizmo aGizmo, Rectangle aOldValue, Rectangle aNewValue )
//    {
//        if( !aOldValue.equals( aNewValue ) )
//        {
//            DisplayRectEvent aEvent = new DisplayRectEvent( aGizmo, aOldValue, aNewValue );
//
//            aGizmo.handleDisplayRectEvent( aEvent );
//        }
//    }
//
//    private void updateClipRect( DisplayRectNode aNode, DisplayRectNode aParent )
//    {
//        if( aNode != null )
//        {
//            Gizmo aGizmo = aNode.getGizmo();
//
//            Rectangle aGizmoRect = aGizmo.isVisible() ?
//                                   Rectangle.create( aGizmo.getWidth(), aGizmo.getHeight() ) :
//                                   Rectangle.EMPTY;
//
//            if( aParent == null ) { aNode.setClipRect( aGizmoRect ); }
//            else
//            {
//                Rectangle aParentClip   = aParent.getClipRect();
//                Rectangle aParentBounds = Rectangle.create( aParentClip.getX     () - aGizmo.getX(),
//                                                            aParentClip.getY     () - aGizmo.getY(),
//                                                            aParentClip.getWidth (),
//                                                            aParentClip.getHeight() );
//
//                aNode.setClipRect( aGizmoRect.intersect( aParentBounds ) );
//            }
//        }
//    }
//
//    private void scheduleLayout( Container aContainer )
//    {
//        // Only take reference identity into account
//        if( mLayingOut != aContainer )
//        {
//            mPendingLayout.add( aContainer );
//        }
//    }
//
//    private void handleAddedGizmo( Gizmo aGizmo )
//    {
//        recordGizmo( aGizmo );
//
//        mUIManager.revalidateUI( aGizmo );
//    }
//
//    private class RenderCommand implements Command
//    {
//        @Override public void execute() { onPaint(); }
//    }
//
//    private class InternalBoundsListener implements BoundsListener
//    {
//        @Override public void boundsChanged( BoundsEvent aBoundsEvent )
//        {
//            EnumSet<Type> aType   = aBoundsEvent.getType();
//            Gizmo         aGizmo  = (Gizmo)aBoundsEvent.getSource();
//            Container     aParent = aGizmo.getParent();
//
//            // Early exit if this event was triggered
//            // by an item as it is being removed from
//            // the container tree.
//            //
//            // Same for invisible items.
//            if( aParent == null || !aGizmo.isVisible() )
//            {
//                return;
//            }
//
//            boolean reRender = false;
//
//            mPendingBoundsChange.add( aGizmo );
//
//            if( aType.contains( Type.Width ) || aType.contains( Type.Height ) )
//            {
//                reRender = true;
//
//                if( aGizmo instanceof Container )
//                {
//                    scheduleLayout( (Container)aGizmo );
//                }
//            }
//
//            if( mDisplay.contains( aParent ) )
//            {
//                aParent.doLayout();
//            }
//            else
//            {
//                scheduleLayout( aParent );
//
//                schedulePaint();
//            }
//
//            if( reRender ) { render( aGizmo, true ); }
//            else           { schedulePaint();        }
//
//            if( mDisplayTree.containsKey( aGizmo ) )
//            {
//                checkDisplayRectChange( aGizmo );
//            }
//        }
//    }
//
//    private class InternalPropertyListener implements PropertyListener
//    {
//        @Override public void propertyChanged( PropertyEvent aPropertyEvent )
//        {
//            String aProperty = aPropertyEvent.getProperty();
//
//            if( aProperty == Gizmo.VISIBLE )
//            {
//                Gizmo     aGizmo  = (Gizmo)aPropertyEvent.getSource();
//                Container aParent = aGizmo.getParent();
//
//                if( mAddedInvisible.contains( aGizmo ) )
//                {
//                    aGizmo.removePropertyListener( mPropertyListener );
//
//                    handleAddedGizmo( aGizmo );
//
//                    mAddedInvisible.remove( aGizmo );
//                }
//
//                if( aParent != null && !mDisplay.contains( aGizmo ) )
//                {
//                    scheduleLayout( aParent );
//
//                    // Gizmos that change bounds while invisible are never scheduled
//                    // for bounds synch, so catch them here
//                    if( aGizmo.isVisible() )
//                    {
//                        mPendingBoundsChange.add( aGizmo );
//                    }
//
//                    mVisibilityChanged.add( aGizmo );
//
//                    render( aParent );
//                }
//                else if( mDisplay.contains( aGizmo ) )
//                {
//                    if( aGizmo.isVisible() )
//                    {
//                        mVisibilityChanged.add  ( aGizmo );
//                        mPendingBoundsChange.add( aGizmo ); // See above
//
//                        render( aGizmo );
//                    }
//                    else
//                    {
//                        GraphicsSurface aScreenContext = GraphicsService.locator().getGraphicsDevice().get( aGizmo );
//
//                        if( aScreenContext != null ) { aScreenContext.setVisible( false ); }
//                    }
//                }
//
//                if( mDisplayTree.containsKey( aGizmo ) ) { checkDisplayRectChange( aGizmo ); }
//            }
//            else if( aProperty == Gizmo.DISPLAYRECT_HANDLING_REQUIRED )
//            {
//                Gizmo aGizmo = (Gizmo)aPropertyEvent.getSource();
//
//                if( aGizmo.getDisplayRectHandlingEnabled() )
//                {
//                    registerDisplayRectMonitoring( aGizmo );
//                }
//                else
//                {
//                    unregisterDisplayRectMonitoring( aGizmo );
//                }
//            }
//            else if( aProperty == Gizmo.IDEAL_SIZE || aProperty == Gizmo.MINIMUM_SIZE )
//            {
//                Container aParent = ((Gizmo)aPropertyEvent.getSource()).getParent();
//
//                if( aParent.getLayout() != null )
//                {
//                    boolean aNeedsLayout = aProperty == Gizmo.IDEAL_SIZE              ?
//                                           aParent.getLayout().usesChildIdealSize  () :
//                                           aParent.getLayout().usesChildMinimumSize();
//
//                    if( aNeedsLayout )
//                    {
//                        if( mDisplay.contains( aParent ) )
//                        {
//                            aParent.doLayout();
//                        }
//                        else
//                        {
//                            scheduleLayout( aParent );
//
//                            schedulePaint();
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private class InternalContainerListener implements ContainerListener
//    {
//        @Override public void itemsAdded( ContainerEvent aContainerEvent )
//        {
//            Collection<Pair<Gizmo,Integer>> aChanges = aContainerEvent.getChanges();
//            Container aContainer                     = aContainerEvent.getSource();
//
//            for( Pair<Gizmo,Integer> aChange : aChanges )
//            {
//                Gizmo aGizmo = aChange.getFirst();
//
//                removeFromCleanupList( aContainer, aGizmo );
//
//                if( aGizmo.isVisible() ) { handleAddedGizmo( aGizmo ); }
//                else
//                {
//                    aGizmo.addPropertyListener( mPropertyListener );
//
//                    mAddedInvisible.add( aGizmo );
//                }
//            }
//
//            if( aContainer.getParent() != null )
//            {
//                aContainer.revalidate();
//            }
//            else { aContainer.doLayout(); }
//        }
//
//        @Override public void itemsRemoved( ContainerEvent aContainerEvent )
//        {
//            Collection<Pair<Gizmo,Integer>> aChanges = aContainerEvent.getChanges();
//            Container aContainer                     = aContainerEvent.getSource();
//
//            if( aContainer.getParent() != null )
//            {
//                for( Pair<Gizmo,Integer> aChange : aChanges )
//                {
//                    addToCleanupList( aContainer, aChange.getFirst() );
//                }
//
//                aContainer.revalidate();
//            }
//            else
//            {
//                for( Pair<Gizmo,Integer> aChange : aChanges )
//                {
//                    releaseResources( aChange.getFirst() );
//
//                    GraphicsService.locator().getGraphicsDevice().release( aChange.getFirst() );
//                }
//            }
//        }
//
//        @Override public void itemsZIndexChanged( ContainerEvent aContainerEvent )
//        {
//            Collection<Pair<Gizmo,Integer>> aChanges = aContainerEvent.getChanges();
//
//            for( Pair<Gizmo,Integer> aChange : aChanges )
//            {
//                GraphicsSurface aSurface = GraphicsService.locator().getGraphicsDevice().get( aChange.getFirst() );
//
//                aSurface.setZIndex( aChange.getSecond() );
//            }
//        }
//    }
//
//    private static class DisplayRectNode
//    {
//        public DisplayRectNode( Gizmo aGizmo )
//        {
//            mGizmo    = aGizmo;
//            mChildren = new ArrayList<DisplayRectNode>();
//        }
//
//        public void add( DisplayRectNode aChild )
//        {
//            aChild.setParent( this   );
//            mChildren.add   ( aChild );
//        }
//
//        public void remove( DisplayRectNode aChild )
//        {
//            aChild.setParent( null   );
//            mChildren.remove( aChild );
//        }
//
//        public void setParent  ( DisplayRectNode aParent   ) { mParent   = aParent;   }
//        public void setClipRect( Rectangle       aClipRect ) { mClipRect = aClipRect; }
//
//        public DisplayRectNode get           ( int i ) { return mChildren.get( i ); }
//        public Gizmo           getGizmo      (       ) { return mGizmo;             }
//        public DisplayRectNode getParent     (       ) { return mParent;            }
//        public Rectangle       getClipRect   (       ) { return mClipRect;          }
//        public int             getNumChildren(       ) { return mChildren.size();   }
//
//
//        private final Gizmo                 mGizmo;
//        private       DisplayRectNode       mParent;
//        private       Rectangle             mClipRect;
//        private final List<DisplayRectNode> mChildren;
//    }
//
//
//
//    // TODO: This may need to be browser specific
//    private static final int                  sPaintDelay = 0;
//
//    private final Display                     mDisplay;
//    private final UIManager                   mUIManager;
//
//    private final Set<Gizmo>                  mGizmos;
//    private       Container                   mLayingOut;
//    private final Set<Gizmo>                  mDirtyGizmos;
//    private final Map<Gizmo, DisplayRectNode> mDisplayTree;
//    private final Set<Gizmo>                  mNeverRendered;
//    private       Set<Container>              mPendingLayout;
//    private final List<Gizmo>                 mPendingRender;
//    private final Map<Container, Set<Gizmo>>  mPendingCleanup;
//    private final Set<Gizmo>                  mAddedInvisible;
//    private final Set<Gizmo>                  mVisibilityChanged;
//    private final Set<Gizmo>                  mPendingBoundsChange;
//
//    private final Task                        mTask;
//    private final InternalBoundsListener      mBoundsListener;
//    private final InternalPropertyListener    mPropertyListener;
//    private final InternalContainerListener   mContainerListener;
//}
