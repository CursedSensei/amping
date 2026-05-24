import { Heart } from 'lucide-react';

interface HeartQuotaProps {
  filled: number;
  total?: number;
}

export default function HeartQuota({ filled, total = 3 }: HeartQuotaProps) {
  return (
    <div className="flex gap-0.5">
      {Array.from({ length: total }).map((_, i) => (
        <Heart
          key={i}
          size={14}
          className={i < filled ? 'text-red-500 fill-red-500' : 'text-gray-300 fill-gray-100'}
        />
      ))}
    </div>
  );
}
