precision mediump float;
varying vec2 ft_Position;
uniform sampler2D vTexture;

void main(){
    gl_FragColor = texture2D(vTexture, ft_Position);
}