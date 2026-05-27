import React, { useEffect, useRef } from 'react';

export default function Gabby() {
  const pupilLeftRef = useRef<SVGGElement>(null);
  const pupilRightRef = useRef<SVGGElement>(null);

  useEffect(() => {
    const maxEyeOffsetX = 7;
    const maxEyeOffsetY = 7;

    const handleMouseMove = (clientX: number, clientY: number) => {
      const windowCenterX = window.innerWidth / 2;
      const windowCenterY = window.innerHeight / 2;

      const normalizedX = (clientX - windowCenterX) / windowCenterX;
      const normalizedY = (clientY - windowCenterY) / windowCenterY;

      const angle = Math.atan2(normalizedY, normalizedX);
      const distance = Math.min(1, Math.hypot(normalizedX, normalizedY));

      const moveX = Math.cos(angle) * distance * maxEyeOffsetX;
      const moveY = Math.sin(angle) * distance * maxEyeOffsetY;

      const transformString = `translate(${moveX}px, ${moveY}px)`;
      
      if (pupilLeftRef.current) pupilLeftRef.current.style.transform = transformString;
      if (pupilRightRef.current) pupilRightRef.current.style.transform = transformString;
    };

    const onMouseMove = (e: MouseEvent) => handleMouseMove(e.clientX, e.clientY);
    const onTouchMove = (e: TouchEvent) => {
      if (e.touches.length > 0) {
        handleMouseMove(e.touches[0].clientX, e.touches[0].clientY);
      }
    };

    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('touchmove', onTouchMove, { passive: true });

    // Initial position
    handleMouseMove(window.innerWidth / 2, window.innerHeight / 2);

    return () => {
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('touchmove', onTouchMove);
    };
  }, []);

  return (
    <div className="relative w-full max-w-[400px] flex-1 flex items-center justify-center pointer-events-auto">
      <style>{`
        /* Smooth path morphing and property transitions */
        .gabby-mouth {
            transition: d 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }
        
        .gabby-mood-transition {
            transition: opacity 0.3s ease, filter 0.5s ease;
        }

        /* Animations */
        @keyframes gabby-breathe {
            0%, 100% { transform: scaleY(1) scaleX(1); }
            50% { transform: scaleY(0.96) scaleX(1.02); }
        }

        .gabby-breathe-anim {
            animation: gabby-breathe 3s ease-in-out infinite;
            transform-origin: 300px 480px;
        }

        /* 3D Waving and Floating Animations */
        @keyframes gabby-waveAndRest {
            0%, 15% { transform: rotate(-60deg); }
            25% { transform: rotate(15deg); }
            35% { transform: rotate(-5deg); }
            45% { transform: rotate(25deg); }
            55% { transform: rotate(-5deg); }
            65% { transform: rotate(25deg); }
            75%, 100% { transform: rotate(-60deg); }
        }

        .gabby-wave-anim {
            animation: gabby-waveAndRest 4s ease-in-out infinite;
            transform-origin: 405px 235px;
        }

        @keyframes gabby-syncBubble {
            0%, 15% { opacity: 0; transform: translateY(10px) scale(0.9); }
            25%, 65% { opacity: 1; transform: translateY(0) scale(1); }
            75%, 100% { opacity: 0; transform: translateY(10px) scale(0.9); }
        }

        .gabby-bubble-anim {
            animation: gabby-syncBubble 4s ease-in-out infinite;
            transform-origin: 470px 160px;
        }
      `}</style>

      <svg viewBox="0 0 600 600" className="w-full h-full overflow-visible drop-shadow-2xl">
        <defs>
            <filter id="gabby-drop-shadow" x="-20%" y="-20%" width="140%" height="140%">
                <feDropShadow dx="0" dy="15" stdDeviation="15" floodColor="#000000" floodOpacity="0.15" />
            </filter>
            
            <radialGradient id="gabby-body-grad" cx="35%" cy="35%" r="65%">
                <stop offset="0%" stopColor="#8ce0ff" />
                <stop offset="45%" stopColor="#4fb5fc" />
                <stop offset="85%" stopColor="#2a85cf" />
                <stop offset="100%" stopColor="#195b96" />
            </radialGradient>

            <radialGradient id="gabby-eye-grad" cx="35%" cy="35%" r="65%">
                <stop offset="0%" stopColor="#ffffff" />
                <stop offset="70%" stopColor="#f0f8ff" />
                <stop offset="100%" stopColor="#caddfa" />
            </radialGradient>
        </defs>

        <ellipse cx="300" cy="495" rx="145" ry="25" fill="rgba(0, 0, 0, 0.1)" style={{ transition: "all 0.5s cubic-bezier(0.4, 0, 0.2, 1)" }} />

        <g style={{ transformOrigin: "300px 480px", transform: "scale(0.95)" }}>
            <g className="gabby-breathe-anim">
                <path d="M 180 280 C 120 180 220 120 320 140 C 430 160 480 200 440 320 C 400 440 350 460 280 440 C 190 420 150 400 180 280 Z" fill="#195b96" filter="url(#gabby-drop-shadow)" transform="translate(5, 10)" />
                
                <g className="gabby-mood-transition">
                    <path d="M 190 280 C 150 300 130 350 160 380 C 180 400 200 380 210 350 Z" fill="url(#gabby-body-grad)" />
                    
                    <path d="M 180 280 C 120 180 220 120 320 140 C 430 160 480 200 440 320 C 400 440 350 460 280 440 C 190 420 150 400 180 280 Z" fill="url(#gabby-body-grad)" />
                    
                    <g className="gabby-wave-anim" style={{ transformOrigin: "405px 235px" }}>
                        <path d="M 400 260 C 450 230 500 180 490 140 C 480 100 440 150 410 210 Z" fill="url(#gabby-body-grad)" />
                    </g>
                </g>

                <g className="gabby-bubble-anim">
                    <path d="M 450 80 A 60 40 0 1 1 570 80 A 60 40 0 0 1 520 115 L 470 160 L 485 115 A 60 40 0 0 1 450 80 Z" fill="white" filter="url(#gabby-drop-shadow)" />
                    <text x="510" y="92" fontFamily="'Nunito', sans-serif" fontSize="32" fontWeight="900" textAnchor="middle" fill="#1d3f5e">Hi!</text>
                </g>

                <circle cx="225" cy="225" r="45" fill="url(#gabby-eye-grad)" />
                <g ref={pupilLeftRef}>
                    <circle cx="225" cy="225" r="24" fill="#1d3f5e" />
                    <circle cx="233" cy="215" r="8" fill="white" />
                    <circle cx="215" cy="235" r="4" fill="white" />
                </g>
                <path d="M 175 225 Q 225 245 275 225 L 275 170 L 175 170 Z" fill="#4fb5fc" opacity="0" className="gabby-mood-transition" />

                <circle cx="345" cy="225" r="45" fill="url(#gabby-eye-grad)" />
                <g ref={pupilRightRef}>
                    <circle cx="345" cy="225" r="28" fill="#1d3f5e" />
                    <circle cx="353" cy="215" r="8" fill="white" />
                    <circle cx="335" cy="235" r="4" fill="white" />
                </g>
                <path d="M 305 225 Q 345 245 385 225 L 385 180 L 305 180 Z" fill="#4fb5fc" opacity="0" className="gabby-mood-transition" />
                
                <path d="M 210 270 Q 210 295 218 295 Q 226 295 226 270 Q 218 255 210 270 Z" fill="#a0e0ff" opacity="0" className="gabby-mood-transition" />

                <path className="gabby-mouth" d="M 300 350 m -35, 0 a 35,35 0 1,0 70,0 a 35,35 0 1,0 -70,0" fill="#1d3f5e" stroke="none" />
                
                <path d="M 285 365 Q 300 380 315 365 Z" fill="#ff6b6b" opacity="0" className="gabby-mood-transition" />
                
                <path d="M 315 385 Q 320 420 315 435 Q 310 420 315 385 Z" fill="#a0e0ff" opacity="0" className="gabby-mood-transition" />
            </g>
        </g>
      </svg>
    </div>
  );
}
