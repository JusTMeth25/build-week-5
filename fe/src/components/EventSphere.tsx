import { Float, MeshDistortMaterial, OrbitControls, Stars } from '@react-three/drei'
import { Canvas, useFrame } from '@react-three/fiber'
import { Suspense, useRef } from 'react'
import type { Mesh } from 'three'

function Core() {
  const ref = useRef<Mesh>(null)
  useFrame((_, delta) => {
    if (ref.current) ref.current.rotation.y += delta * 0.35
  })
  return (
    <Float speed={2.2} rotationIntensity={1.1} floatIntensity={1.6}>
      <mesh ref={ref} scale={1.25}>
        <icosahedronGeometry args={[1.75, 5]} />
        <MeshDistortMaterial color="#22d3ee" emissive="#4f46e5" emissiveIntensity={0.55} roughness={0.18} metalness={0.55} distort={0.33} speed={2.4} />
      </mesh>
      <mesh scale={2.4}>
        <torusGeometry args={[1.15, 0.012, 16, 140]} />
        <meshBasicMaterial color="#f0abfc" transparent opacity={0.75} />
      </mesh>
      <mesh rotation={[1.2, 0.2, 0.7]} scale={2.85}>
        <torusGeometry args={[1.15, 0.01, 16, 140]} />
        <meshBasicMaterial color="#67e8f9" transparent opacity={0.5} />
      </mesh>
    </Float>
  )
}

export function EventSphere() {
  return (
    <div className="pointer-events-none absolute inset-0 -z-0 opacity-80">
      <Canvas camera={{ position: [0, 0, 6], fov: 45 }}>
        <Suspense fallback={null}>
          <ambientLight intensity={0.7} />
          <pointLight position={[5, 5, 5]} intensity={55} color="#22d3ee" />
          <pointLight position={[-5, -3, 3]} intensity={35} color="#f472b6" />
          <Stars radius={55} depth={30} count={900} factor={3} fade speed={1} />
          <Core />
          <OrbitControls enableZoom={false} enablePan={false} autoRotate autoRotateSpeed={0.6} />
        </Suspense>
      </Canvas>
    </div>
  )
}