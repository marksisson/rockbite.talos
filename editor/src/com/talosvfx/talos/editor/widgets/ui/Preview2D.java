package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.wrappers.IDragPointProvider;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.render.ParticleRenderer;
import com.talosvfx.talos.runtime.render.SpriteBatchParticleRenderer;

public class Preview2D extends PreviewWidget {

    private ParticleRenderer particleRenderer;

    private SpriteBatchParticleRenderer spriteBatchParticleRenderer;

    private ShapeRenderer shapeRenderer;

    private Color tmpColor = new Color();
    private float gridSize;

    private PreviewImageControllerWidget previewController;

    private Image previewImage = new Image();

    private Array<DragPoint> dragPoints = new Array<>();
    private IDragPointProvider dragPointProvider = null;

    private String backgroundImagePath = "";

    public Preview2D() {
        super();

        setWorldSize(10f);
        gridSize = 1f;

        spriteBatchParticleRenderer = new SpriteBatchParticleRenderer(null);
        particleRenderer = spriteBatchParticleRenderer;
        shapeRenderer = new ShapeRenderer();

        cameraController.scrollOnly = true; // camera controller can't operate in this shitty custom conditions

        addListener(new InputListener() {

            boolean moving = false;
            private Vector3 tmp = new Vector3();
            private Vector3 tmp2 = new Vector3();
            private Vector2 prevPos = new Vector2();
            private Vector2 pos = new Vector2();

            private DragPoint currentlyDragging = null;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                moving = false;
                getWorldFromLocal(tmp.set(x, y, 0));
                pos.set(tmp.x, tmp.y);
                prevPos.set(x, y);

                //detect drag points
                for(DragPoint point: dragPoints) {
                    if(pos.dst(point.position) < 0.2f * camera.zoom) {
                        // dragging a point
                        currentlyDragging = point;
                        return true;
                    }
                }

                if(button == 1) {
                    moving = true;
                    return true;
                }
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);

                getWorldFromLocal(tmp.set(x, y, 0)); // I can't really explain
                pos.set(tmp.x, tmp.y);

                if(moving) {
                    final ParticleEffectInstance particleEffect = TalosMain.Instance().TalosProject().getParticleEffect();
                    particleEffect.setPosition(tmp.x, tmp.y);
                } else {
                    getWorldFromLocal(tmp.set(prevPos.x, prevPos.y, 0));
                    getWorldFromLocal(tmp2.set(x, y, 0));

                    if(currentlyDragging == null) {
                        // panning

                        camera.position.sub(tmp2.x-tmp.x, tmp2.y-tmp.y, 0);
                    } else {
                        // dragging a point
                        currentlyDragging.position.add(tmp2.x-tmp.x, tmp2.y-tmp.y);
                        dragPointProvider.dragPointChanged(currentlyDragging);
                    }
                }

                prevPos.set(x, y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                currentlyDragging = null;
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                return super.keyUp(event, keycode);
            }
        });
    }

    @Override
    protected Table buildPreviewController() {
        previewController = new PreviewImageControllerWidget(TalosMain.Instance().getSkin()) {
            @Override
            public void removeImage () {
                super.removeImage();
                previewImage.setDrawable(null);
                backgroundImagePath = "";
            }

            @Override
            public void gridSizeChanged(float size) {
                super.gridSizeChanged(size);
                setWorldSize(10f * size);
                gridSize = size;
                resetCamera();
            }
        };

        return previewController;
    }

    public void fileDrop (float x, float y, String[] paths) {
        temp.set(x, y);
        (getStage().getViewport()).unproject(temp);
        stageToLocalCoordinates(temp);
        if(this.hit(temp.x, temp.y, false) != null) {
            addPreviewImage(paths);
        }
    }

    public void addPreviewImage (String[] paths) {
        if (paths.length == 1) {

            String resourcePath = paths[0];
            FileHandle fileHandle = Gdx.files.absolute(resourcePath);

            final String extension = fileHandle.extension();

            if (extension.endsWith("png") || extension.endsWith("jpg")) {
                fileHandle = TalosMain.Instance().ProjectController().findFile(fileHandle);
                if(fileHandle != null && fileHandle.exists()) {
                    final TextureRegion textureRegion = new TextureRegion(new Texture(fileHandle));

                    if (textureRegion != null) {
                        previewImage.setDrawable(new TextureRegionDrawable(textureRegion));
                        previewController.setImageWidth(10);

                        backgroundImagePath = fileHandle.path();

                        TalosMain.Instance().ProjectController().setDirty();
                    }
                }
            }
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        //stupid hack, plz do it normal way
        if(Preview2D.this.hasScrollFocus() && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            resetCamera();
        }
    }

    @Override
    public void drawContent(Batch batch, float parentAlpha) {
        super.drawContent(batch, parentAlpha);
        if (previewController.isGridVisible()) {
            batch.end();
            drawGrid(batch, parentAlpha * 0.5f);
            batch.begin();
        }

        mid.set(0, 0);

        float imagePrefWidth = previewImage.getPrefWidth();
        float imagePrefHeight = previewImage.getPrefHeight();
        float scale = imagePrefHeight / imagePrefWidth;

        float imageWidth = previewController.getImageWidth();
        float imageHeight = imageWidth * scale;
        previewController.getPreviewBoxWidth();

        previewImage.setPosition(mid.x - imageWidth / 2, mid.y - imageHeight / 2);
        previewImage.setSize(imageWidth, imageHeight);
        if (previewController.isBackground()) {
            previewImage.draw(batch, parentAlpha);
        }

        spriteBatchParticleRenderer.setBatch(batch);

        batch.flush();
        glProfiler.enable();

        long timeBefore = TimeUtils.nanoTime();

        final ParticleEffectInstance particleEffect = TalosMain.Instance().TalosProject().getParticleEffect();
        particleEffect.render(particleRenderer);

        batch.flush();
        renderTime.put(TimeUtils.timeSinceNanos(timeBefore));
        trisCount = (int) (glProfiler.getVertexCount().value / 3f);
        glProfiler.disable();


        if (!previewController.isBackground()) {
            previewImage.draw(batch, parentAlpha);
        }

        // now for the drag points
        if(dragPoints.size > 0) {
            batch.end();
            tmpColor.set(Color.ORANGE);
            tmpColor.a = 0.8f;
            Gdx.gl.glLineWidth(1f);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(tmpColor);

            for (DragPoint point : dragPoints) {
                shapeRenderer.circle(point.position.x, point.position.y, 0.1f * camera.zoom, 15);
            }

            shapeRenderer.end();
            batch.begin();
        }
    }

    public void registerForDragPoints(IDragPointProvider dragPointProvider) {
        this.dragPointProvider = dragPointProvider;
        DragPoint[] arr = dragPointProvider.fetchDragPoints();
        dragPoints.clear();
        for(int i = 0; i < arr.length; i++) {
            dragPoints.add(arr[i]);
        }
    }

    public void unregisterDragPoints() {
        this.dragPointProvider = null;
        dragPoints.clear();
    }

    public void unregisterDragPoints(IDragPointProvider dragPointProvider) {
        if(this.dragPointProvider == dragPointProvider) {
            this.dragPointProvider = null;
            dragPoints.clear();
        }
    }

    public String getBackgroundImagePath() {
        return backgroundImagePath;
    }

    public boolean isBackgroundImageInBack() {
        return previewController.isBackground();
    }

    public void setImageIsBackground(boolean isBackground) {
        previewController.setIsBackground(isBackground);
    }


    public boolean isGridVisible() {
        return previewController.isGridVisible();
    }

    public void setGridVisible(boolean isVisible) {
        previewController.setGridVisible(isVisible);
    }

    public float getBgImageSize() {
        return previewController.getImageWidth();
    }

    public void setBgImageSize(float size) {
        previewController.setImageWidth(size);
    }

    public void setBackgroundImage(String bgImagePath) {
        if(bgImagePath != null) {
            addPreviewImage(new String[] {bgImagePath});
        } else {
            previewImage.setDrawable(null);
            backgroundImagePath = "";
        }
    }

    public void resetToDefaults() {
        previewImage.setDrawable(null);
        backgroundImagePath = "";
        setBgImageSize(10f);
        setImageIsBackground(true);
        setCameraZoom(1.4285715f);
        setCameraPos(0, 0);
        unregisterDragPoints();
    }

    public float getGridSize() {
        return gridSize;
    }

    public void setGridSize(float gridSize) {
        previewController.setGridSize(gridSize);
        setWorldSize(10f * gridSize);
        this.gridSize = gridSize;
    }
}